/* 
 * Copyright (c) HANIUM SOLOLAW 
 */
package com.hanium.sololaw.domain.notification.scheduler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.hanium.sololaw.domain.cases.entity.Case;
import com.hanium.sololaw.domain.cases.entity.CaseTodo;
import com.hanium.sololaw.domain.cases.repository.CaseRepository;
import com.hanium.sololaw.domain.cases.repository.CaseTodoRepository;
import com.hanium.sololaw.domain.evidence.entity.Evidence;
import com.hanium.sololaw.domain.evidence.entity.enums.EvidenceStatus;
import com.hanium.sololaw.domain.evidence.repository.EvidenceRepository;
import com.hanium.sololaw.domain.notification.entity.Notification;
import com.hanium.sololaw.domain.notification.entity.NotificationSetting;
import com.hanium.sololaw.domain.notification.repository.NotificationRepository;
import com.hanium.sololaw.domain.notification.repository.NotificationSettingRepository;
import com.hanium.sololaw.domain.schedule.entity.Schedule;
import com.hanium.sololaw.domain.schedule.entity.enums.ScheduleType;
import com.hanium.sololaw.domain.schedule.repository.ScheduleRepository;

@ExtendWith(MockitoExtension.class)
class NotificationSettingReminderSchedulerTest {

  @Mock private ScheduleRepository scheduleRepository;
  @Mock private CaseRepository caseRepository;
  @Mock private CaseTodoRepository caseTodoRepository;
  @Mock private EvidenceRepository evidenceRepository;
  @Mock private NotificationSettingRepository notificationSettingRepository;
  @Mock private NotificationRepository notificationRepository;

  @InjectMocks private NotificationSettingReminderScheduler scheduler;

  @Test
  void sendGlobalReminders_createsHearingNotification_whenEnabled() {
    Schedule schedule =
        Schedule.builder()
            .id(1L)
            .userId(1L)
            .caseId(5L)
            .title("변론기일")
            .scheduleType(ScheduleType.HEARING)
            .eventDate(LocalDate.now().plusDays(7))
            .build();
    when(scheduleRepository.findAllDueForGlobalReminder(any(), any(), any()))
        .thenReturn(List.of(schedule));
    when(caseTodoRepository.findAllByIsDoneFalseAndDueDateBeforeAndOverdueAlertSentAtIsNull(any()))
        .thenReturn(List.of());
    when(evidenceRepository.findAllByStatusAndSupplementAlertSentAtIsNull(any()))
        .thenReturn(List.of());
    when(notificationSettingRepository.findByUserId(1L))
        .thenReturn(Optional.of(NotificationSetting.builder().userId(1L).build()));

    scheduler.sendGlobalReminders();

    verify(notificationRepository).save(any(Notification.class));
    assertThat(schedule.getGlobalReminderSentDate()).isEqualTo(LocalDate.now());
  }

  @Test
  void sendGlobalReminders_skipsHearingNotification_whenAlertDisabled() {
    Schedule schedule =
        Schedule.builder()
            .id(1L)
            .userId(1L)
            .caseId(5L)
            .title("변론기일")
            .scheduleType(ScheduleType.HEARING)
            .eventDate(LocalDate.now().plusDays(3))
            .build();
    when(scheduleRepository.findAllDueForGlobalReminder(any(), any(), any()))
        .thenReturn(List.of(schedule));
    when(caseTodoRepository.findAllByIsDoneFalseAndDueDateBeforeAndOverdueAlertSentAtIsNull(any()))
        .thenReturn(List.of());
    when(evidenceRepository.findAllByStatusAndSupplementAlertSentAtIsNull(any()))
        .thenReturn(List.of());
    when(notificationSettingRepository.findByUserId(1L))
        .thenReturn(
            Optional.of(
                NotificationSetting.builder().userId(1L).hearingReminderAlert(false).build()));

    scheduler.sendGlobalReminders();

    verify(notificationRepository, never()).save(any(Notification.class));
    assertThat(schedule.getGlobalReminderSentDate()).isNull();
  }

  @Test
  void sendGlobalReminders_createsDeadlineNotification_forSubmissionDeadlineType() {
    Schedule schedule =
        Schedule.builder()
            .id(2L)
            .userId(1L)
            .caseId(5L)
            .title("준비서면 제출기한")
            .scheduleType(ScheduleType.SUBMISSION_DEADLINE)
            .eventDate(LocalDate.now().plusDays(1))
            .build();
    when(scheduleRepository.findAllDueForGlobalReminder(any(), any(), any()))
        .thenReturn(List.of(schedule));
    when(caseTodoRepository.findAllByIsDoneFalseAndDueDateBeforeAndOverdueAlertSentAtIsNull(any()))
        .thenReturn(List.of());
    when(evidenceRepository.findAllByStatusAndSupplementAlertSentAtIsNull(any()))
        .thenReturn(List.of());
    when(notificationSettingRepository.findByUserId(1L))
        .thenReturn(Optional.of(NotificationSetting.builder().userId(1L).build()));

    scheduler.sendGlobalReminders();

    verify(notificationRepository).save(any(Notification.class));
    assertThat(schedule.getGlobalReminderSentDate()).isEqualTo(LocalDate.now());
  }

  @Test
  void sendGlobalReminders_createsOverdueTodoNotification_whenEnabled() {
    CaseTodo todo =
        CaseTodo.builder()
            .id(10L)
            .caseId(5L)
            .title("답변서 제출")
            .dueDate(LocalDate.now().minusDays(1))
            .isDone(false)
            .build();
    when(scheduleRepository.findAllDueForGlobalReminder(any(), any(), any())).thenReturn(List.of());
    when(caseTodoRepository.findAllByIsDoneFalseAndDueDateBeforeAndOverdueAlertSentAtIsNull(any()))
        .thenReturn(List.of(todo));
    when(evidenceRepository.findAllByStatusAndSupplementAlertSentAtIsNull(any()))
        .thenReturn(List.of());
    when(caseRepository.findAllById(List.of(5L)))
        .thenReturn(List.of(Case.builder().id(5L).userId(1L).build()));
    when(notificationSettingRepository.findByUserId(1L))
        .thenReturn(Optional.of(NotificationSetting.builder().userId(1L).build()));

    scheduler.sendGlobalReminders();

    verify(notificationRepository).save(any(Notification.class));
    assertThat(todo.getOverdueAlertSentAt()).isNotNull();
  }

  @Test
  void sendGlobalReminders_skipsOverdueTodoNotification_whenAlertDisabled() {
    CaseTodo todo =
        CaseTodo.builder()
            .id(10L)
            .caseId(5L)
            .title("답변서 제출")
            .dueDate(LocalDate.now().minusDays(1))
            .isDone(false)
            .build();
    when(scheduleRepository.findAllDueForGlobalReminder(any(), any(), any())).thenReturn(List.of());
    when(caseTodoRepository.findAllByIsDoneFalseAndDueDateBeforeAndOverdueAlertSentAtIsNull(any()))
        .thenReturn(List.of(todo));
    when(evidenceRepository.findAllByStatusAndSupplementAlertSentAtIsNull(any()))
        .thenReturn(List.of());
    when(caseRepository.findAllById(List.of(5L)))
        .thenReturn(List.of(Case.builder().id(5L).userId(1L).build()));
    when(notificationSettingRepository.findByUserId(1L))
        .thenReturn(
            Optional.of(
                NotificationSetting.builder().userId(1L).overdueDeadlineAlert(false).build()));

    scheduler.sendGlobalReminders();

    verify(notificationRepository, never()).save(any(Notification.class));
    assertThat(todo.getOverdueAlertSentAt()).isNull();
  }

  @Test
  void sendGlobalReminders_createsEvidenceSupplementNotification_whenEnabled() {
    Evidence evidence =
        Evidence.builder()
            .id(20L)
            .caseId(5L)
            .fileName("계약서.pdf")
            .status(EvidenceStatus.NEEDS_SUPPLEMENT)
            .build();
    when(scheduleRepository.findAllDueForGlobalReminder(any(), any(), any())).thenReturn(List.of());
    when(caseTodoRepository.findAllByIsDoneFalseAndDueDateBeforeAndOverdueAlertSentAtIsNull(any()))
        .thenReturn(List.of());
    when(evidenceRepository.findAllByStatusAndSupplementAlertSentAtIsNull(
            EvidenceStatus.NEEDS_SUPPLEMENT))
        .thenReturn(List.of(evidence));
    when(caseRepository.findAllById(List.of(5L)))
        .thenReturn(List.of(Case.builder().id(5L).userId(1L).build()));
    when(notificationSettingRepository.findByUserId(1L))
        .thenReturn(Optional.of(NotificationSetting.builder().userId(1L).build()));

    scheduler.sendGlobalReminders();

    verify(notificationRepository).save(any(Notification.class));
    assertThat(evidence.getSupplementAlertSentAt()).isNotNull();
  }

  @Test
  void sendGlobalReminders_skipsEvidenceSupplementNotification_whenAlertDisabled() {
    Evidence evidence =
        Evidence.builder()
            .id(20L)
            .caseId(5L)
            .fileName("계약서.pdf")
            .status(EvidenceStatus.NEEDS_SUPPLEMENT)
            .build();
    when(scheduleRepository.findAllDueForGlobalReminder(any(), any(), any())).thenReturn(List.of());
    when(caseTodoRepository.findAllByIsDoneFalseAndDueDateBeforeAndOverdueAlertSentAtIsNull(any()))
        .thenReturn(List.of());
    when(evidenceRepository.findAllByStatusAndSupplementAlertSentAtIsNull(
            EvidenceStatus.NEEDS_SUPPLEMENT))
        .thenReturn(List.of(evidence));
    when(caseRepository.findAllById(List.of(5L)))
        .thenReturn(List.of(Case.builder().id(5L).userId(1L).build()));
    when(notificationSettingRepository.findByUserId(1L))
        .thenReturn(
            Optional.of(
                NotificationSetting.builder().userId(1L).evidenceSupplementAlert(false).build()));

    scheduler.sendGlobalReminders();

    verify(notificationRepository, never()).save(any(Notification.class));
    assertThat(evidence.getSupplementAlertSentAt()).isNull();
  }

  @Test
  void sendGlobalReminders_skipsGracefully_whenNotificationSettingMissing() {
    Schedule schedule =
        Schedule.builder()
            .id(1L)
            .userId(1L)
            .caseId(5L)
            .title("변론기일")
            .scheduleType(ScheduleType.HEARING)
            .eventDate(LocalDate.now().plusDays(1))
            .build();
    when(scheduleRepository.findAllDueForGlobalReminder(any(), any(), any()))
        .thenReturn(List.of(schedule));
    when(caseTodoRepository.findAllByIsDoneFalseAndDueDateBeforeAndOverdueAlertSentAtIsNull(any()))
        .thenReturn(List.of());
    when(evidenceRepository.findAllByStatusAndSupplementAlertSentAtIsNull(any()))
        .thenReturn(List.of());
    when(notificationSettingRepository.findByUserId(1L)).thenReturn(Optional.empty());

    scheduler.sendGlobalReminders();

    verify(notificationRepository, never()).save(any(Notification.class));
    assertThat(schedule.getGlobalReminderSentDate()).isNull();
  }
}
