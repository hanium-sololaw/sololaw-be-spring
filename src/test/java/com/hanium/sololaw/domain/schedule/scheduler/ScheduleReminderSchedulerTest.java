/* 
 * Copyright (c) HANIUM SOLOLAW 
 */
package com.hanium.sololaw.domain.schedule.scheduler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.hanium.sololaw.domain.notification.entity.Notification;
import com.hanium.sololaw.domain.notification.repository.NotificationRepository;
import com.hanium.sololaw.domain.schedule.entity.Schedule;
import com.hanium.sololaw.domain.schedule.entity.enums.ReminderUnit;
import com.hanium.sololaw.domain.schedule.entity.enums.ScheduleType;
import com.hanium.sololaw.domain.schedule.repository.ScheduleRepository;

@ExtendWith(MockitoExtension.class)
class ScheduleReminderSchedulerTest {

  @Mock private ScheduleRepository scheduleRepository;
  @Mock private NotificationRepository notificationRepository;

  @InjectMocks private ScheduleReminderScheduler scheduler;

  @Test
  void sendDueReminders_createsNotification_whenTriggerTimeHasPassed() {
    LocalDateTime tomorrow = LocalDateTime.now().plusDays(1);
    Schedule schedule =
        Schedule.builder()
            .id(1L)
            .userId(1L)
            .caseId(5L)
            .title("변론기일")
            .scheduleType(ScheduleType.HEARING)
            .eventDate(tomorrow.toLocalDate())
            .eventTime(tomorrow.toLocalTime())
            .reminderEnabled(true)
            .reminderValue(2)
            .reminderUnit(ReminderUnit.DAY)
            .build();
    when(scheduleRepository.findAllByReminderEnabledTrueAndReminderSentAtIsNull())
        .thenReturn(List.of(schedule));

    scheduler.sendDueReminders();

    verify(notificationRepository).save(any(Notification.class));
    assertThat(schedule.getReminderSentAt()).isNotNull();
  }

  @Test
  void sendDueReminders_skips_whenTriggerTimeNotYetReached() {
    LocalDate farFuture = LocalDate.now().plusDays(30);
    Schedule schedule =
        Schedule.builder()
            .id(2L)
            .userId(1L)
            .title("준비서면 제출기한")
            .eventDate(farFuture)
            .eventTime(LocalTime.NOON)
            .reminderEnabled(true)
            .reminderValue(1)
            .reminderUnit(ReminderUnit.DAY)
            .build();
    when(scheduleRepository.findAllByReminderEnabledTrueAndReminderSentAtIsNull())
        .thenReturn(List.of(schedule));

    scheduler.sendDueReminders();

    verify(notificationRepository, never()).save(any(Notification.class));
    assertThat(schedule.getReminderSentAt()).isNull();
  }

  @Test
  void sendDueReminders_skips_whenReminderValueOrUnitMissing() {
    Schedule schedule =
        Schedule.builder()
            .id(3L)
            .userId(1L)
            .title("일정")
            .eventDate(LocalDate.now())
            .reminderEnabled(true)
            .build();
    when(scheduleRepository.findAllByReminderEnabledTrueAndReminderSentAtIsNull())
        .thenReturn(List.of(schedule));

    scheduler.sendDueReminders();

    verify(notificationRepository, never()).save(any(Notification.class));
  }
}
