/* 
 * Copyright (c) HANIUM SOLOLAW 
 */
package com.hanium.sololaw.domain.notification.scheduler;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.hanium.sololaw.domain.cases.entity.Case;
import com.hanium.sololaw.domain.cases.entity.CaseTodo;
import com.hanium.sololaw.domain.cases.repository.CaseRepository;
import com.hanium.sololaw.domain.cases.repository.CaseTodoRepository;
import com.hanium.sololaw.domain.evidence.entity.Evidence;
import com.hanium.sololaw.domain.evidence.entity.enums.EvidenceStatus;
import com.hanium.sololaw.domain.evidence.repository.EvidenceRepository;
import com.hanium.sololaw.domain.notification.entity.Notification;
import com.hanium.sololaw.domain.notification.entity.NotificationSetting;
import com.hanium.sololaw.domain.notification.entity.enums.NotificationType;
import com.hanium.sololaw.domain.notification.repository.NotificationRepository;
import com.hanium.sololaw.domain.notification.repository.NotificationSettingRepository;
import com.hanium.sololaw.domain.schedule.entity.Schedule;
import com.hanium.sololaw.domain.schedule.entity.enums.ScheduleType;
import com.hanium.sololaw.domain.schedule.repository.ScheduleRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * notification_settings 토글 기반 전역 리마인더 배치. 사용자가 개별 일정에 건 리마인더(ScheduleReminderScheduler)와 달리, 사용자별
 * 알림 설정 4종(기일·제출기한·기한지남·증거보완)을 검사해 알림을 생성한다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationSettingReminderScheduler {

  private static final List<Integer> HEARING_REMINDER_DAYS_BEFORE = List.of(7, 3, 1);

  private final ScheduleRepository scheduleRepository;
  private final CaseRepository caseRepository;
  private final CaseTodoRepository caseTodoRepository;
  private final EvidenceRepository evidenceRepository;
  private final NotificationSettingRepository notificationSettingRepository;
  private final NotificationRepository notificationRepository;

  @Scheduled(cron = "0 0 9 * * *")
  @Transactional
  public void sendGlobalReminders() {
    Map<Long, NotificationSetting> settingCache = new HashMap<>();
    int hearingAndDeadlineCount = sendHearingAndDeadlineReminders(settingCache);
    int overdueCount = sendOverdueTodoReminders(settingCache);
    int supplementCount = sendEvidenceSupplementReminders(settingCache);

    if (hearingAndDeadlineCount + overdueCount + supplementCount > 0) {
      log.info(
          "[NotificationSettingReminderScheduler] sendGlobalReminders() - 기일·제출기한 {}건, 기한지남 {}건,"
              + " 증거보완 {}건 발송",
          hearingAndDeadlineCount,
          overdueCount,
          supplementCount);
    }
  }

  private int sendHearingAndDeadlineReminders(Map<Long, NotificationSetting> settingCache) {
    LocalDate today = LocalDate.now();
    List<LocalDate> targetDates =
        HEARING_REMINDER_DAYS_BEFORE.stream().map(today::plusDays).toList();
    List<Schedule> candidates = scheduleRepository.findAllDueForGlobalReminder(targetDates, today);

    int sentCount = 0;
    for (Schedule schedule : candidates) {
      NotificationSetting setting = getSetting(schedule.getUserId(), settingCache);
      if (setting == null) {
        continue;
      }
      /*
         HEARING만 기일 토글을 타고, 나머지 전체(SUBMISSION_DEADLINE 포함 화면의 다른 일정 유형들)는
         전부 제출기한 토글로 묶는다 — scheduleType으로 후보를 좁히지 않았으므로 모든 유형이 여기로 들어온다.
      */
      boolean enabled =
          schedule.getScheduleType() == ScheduleType.HEARING
              ? setting.getHearingReminderAlert()
              : setting.getSubmissionDeadlineAlert();
      if (!enabled) {
        continue;
      }

      long daysBefore = ChronoUnit.DAYS.between(today, schedule.getEventDate());
      notificationRepository.save(
          Notification.builder()
              .userId(schedule.getUserId())
              .relatedCaseId(schedule.getCaseId())
              .type(NotificationType.fromScheduleType(schedule.getScheduleType()))
              .title("%s D-%d".formatted(schedule.getTitle(), daysBefore))
              .content(
                  "%s 일정이 %d일 남았습니다. (%s)"
                      .formatted(schedule.getTitle(), daysBefore, schedule.getEventDate()))
              .build());
      schedule.markGlobalReminderSent(today);
      sentCount++;
    }
    return sentCount;
  }

  private int sendOverdueTodoReminders(Map<Long, NotificationSetting> settingCache) {
    LocalDate today = LocalDate.now();
    List<CaseTodo> overdueTodos =
        caseTodoRepository.findAllByIsDoneFalseAndDueDateBeforeAndOverdueAlertSentAtIsNull(today);
    if (overdueTodos.isEmpty()) {
      return 0;
    }

    Map<Long, Long> caseIdToUserId =
        resolveCaseOwners(overdueTodos.stream().map(CaseTodo::getCaseId).toList());

    int sentCount = 0;
    for (CaseTodo todo : overdueTodos) {
      Long userId = caseIdToUserId.get(todo.getCaseId());
      if (userId == null) {
        continue;
      }
      NotificationSetting setting = getSetting(userId, settingCache);
      if (setting == null) {
        continue;
      }
      if (!setting.getOverdueDeadlineAlert()) {
        continue;
      }

      notificationRepository.save(
          Notification.builder()
              .userId(userId)
              .relatedCaseId(todo.getCaseId())
              .type(NotificationType.DEADLINE)
              .title("%s 기한이 지났습니다".formatted(todo.getTitle()))
              .content(
                  "%s 할 일의 기한(%s)이 지났는데 아직 완료 표시가 없습니다."
                      .formatted(todo.getTitle(), todo.getDueDate()))
              .build());
      todo.markOverdueAlertSent();
      sentCount++;
    }
    return sentCount;
  }

  private int sendEvidenceSupplementReminders(Map<Long, NotificationSetting> settingCache) {
    List<Evidence> needsSupplementList =
        evidenceRepository.findAllByStatusAndSupplementAlertSentAtIsNull(
            EvidenceStatus.NEEDS_SUPPLEMENT);
    if (needsSupplementList.isEmpty()) {
      return 0;
    }

    Map<Long, Long> caseIdToUserId =
        resolveCaseOwners(needsSupplementList.stream().map(Evidence::getCaseId).toList());

    int sentCount = 0;
    for (Evidence evidence : needsSupplementList) {
      Long userId = caseIdToUserId.get(evidence.getCaseId());
      if (userId == null) {
        continue;
      }
      NotificationSetting setting = getSetting(userId, settingCache);
      if (setting == null) {
        continue;
      }
      if (!setting.getEvidenceSupplementAlert()) {
        continue;
      }

      notificationRepository.save(
          Notification.builder()
              .userId(userId)
              .relatedCaseId(evidence.getCaseId())
              .type(NotificationType.EVIDENCE_SUPPLEMENT)
              .title("%s 보완이 필요합니다".formatted(evidence.getFileName()))
              .content("%s 증거가 보완필요 상태로 남아 있습니다.".formatted(evidence.getFileName()))
              .build());
      evidence.markSupplementAlertSent();
      sentCount++;
    }
    return sentCount;
  }

  private NotificationSetting getSetting(Long userId, Map<Long, NotificationSetting> cache) {
    if (cache.containsKey(userId)) {
      return cache.get(userId);
    }
    NotificationSetting setting = notificationSettingRepository.findByUserId(userId).orElse(null);
    cache.put(userId, setting);
    return setting;
  }

  private Map<Long, Long> resolveCaseOwners(List<Long> caseIds) {
    List<Long> distinctCaseIds = caseIds.stream().distinct().toList();
    if (distinctCaseIds.isEmpty()) {
      return Map.of();
    }
    Map<Long, Long> caseIdToUserId = new HashMap<>();
    for (Case c : caseRepository.findAllById(distinctCaseIds)) {
      caseIdToUserId.put(c.getId(), c.getUserId());
    }
    return caseIdToUserId;
  }
}
