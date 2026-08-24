/* 
 * Copyright (c) HANIUM SOLOLAW 
 */
package com.hanium.sololaw.domain.schedule.scheduler;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.hanium.sololaw.domain.notification.entity.Notification;
import com.hanium.sololaw.domain.notification.entity.enums.NotificationType;
import com.hanium.sololaw.domain.notification.repository.NotificationRepository;
import com.hanium.sololaw.domain.schedule.entity.Schedule;
import com.hanium.sololaw.domain.schedule.entity.enums.ScheduleType;
import com.hanium.sololaw.domain.schedule.repository.ScheduleRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 일정별 개별 리마인더(reminder_enabled=true)를 검사해 알림을 생성하는 배치. 전역 notification_settings 기반(기일 7일 전·3일 전·1일
 * 전 고정) 알림은 이 스케줄러의 범위가 아니다, 별도 배치로 구현 예정.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ScheduleReminderScheduler {

  private final ScheduleRepository scheduleRepository;
  private final NotificationRepository notificationRepository;

  @Scheduled(cron = "0 */10 * * * *")
  @Transactional
  public void sendDueReminders() {
    List<Schedule> candidates =
        scheduleRepository.findAllByReminderEnabledTrueAndReminderSentAtIsNull();
    LocalDateTime now = LocalDateTime.now();
    int sentCount = 0;

    for (Schedule schedule : candidates) {
      if (schedule.getReminderValue() == null || schedule.getReminderUnit() == null) {
        continue;
      }
      if (computeTriggerAt(schedule).isAfter(now)) {
        continue;
      }

      notificationRepository.save(
          Notification.builder()
              .userId(schedule.getUserId())
              .relatedCaseId(schedule.getCaseId())
              .type(resolveNotificationType(schedule.getScheduleType()))
              .title("%s 리마인더".formatted(schedule.getTitle()))
              .content(
                  "%s 일정이 곧 도래합니다. (%s)".formatted(schedule.getTitle(), schedule.getEventDate()))
              .build());
      schedule.markReminderSent();
      sentCount++;
    }

    if (sentCount > 0) {
      log.info("[ScheduleReminderScheduler] sendDueReminders() - {}건 발송", sentCount);
    }
  }

  private LocalDateTime computeTriggerAt(Schedule schedule) {
    LocalDateTime eventDateTime =
        LocalDateTime.of(
            schedule.getEventDate(),
            schedule.getEventTime() != null ? schedule.getEventTime() : LocalTime.MIDNIGHT);
    return switch (schedule.getReminderUnit()) {
      case DAY -> eventDateTime.minusDays(schedule.getReminderValue());
      case HOUR -> eventDateTime.minusHours(schedule.getReminderValue());
    };
  }

  private NotificationType resolveNotificationType(ScheduleType scheduleType) {
    return scheduleType == ScheduleType.HEARING
        ? NotificationType.HEARING
        : NotificationType.DEADLINE;
  }
}
