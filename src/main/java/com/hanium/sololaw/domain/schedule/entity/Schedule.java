/* 
 * Copyright (c) HANIUM SOLOLAW 
 */
package com.hanium.sololaw.domain.schedule.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import jakarta.persistence.*;

import com.hanium.sololaw.domain.schedule.entity.enums.ReminderUnit;
import com.hanium.sololaw.domain.schedule.entity.enums.ScheduleType;
import com.hanium.sololaw.global.common.BaseTimeEntity;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "schedules")
public class Schedule extends BaseTimeEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "user_id", nullable = false)
  private Long userId;

  @Column(name = "case_id")
  private Long caseId;

  @Column(nullable = false, length = 200)
  private String title;

  @Enumerated(EnumType.STRING)
  @Column(length = 20)
  private ScheduleType scheduleType;

  @Column(nullable = false)
  private LocalDate eventDate;

  private LocalTime eventTime;

  @Column(length = 200)
  private String location;

  @Column(columnDefinition = "TEXT")
  private String memo;

  @Builder.Default
  @Column(nullable = false)
  private Boolean reminderEnabled = false;

  private Integer reminderValue;

  @Enumerated(EnumType.STRING)
  @Column(length = 10)
  private ReminderUnit reminderUnit;

  private LocalDateTime reminderSentAt;

  public void update(
      String title,
      ScheduleType scheduleType,
      LocalDate eventDate,
      LocalTime eventTime,
      String location,
      String memo,
      Boolean reminderEnabled,
      Integer reminderValue,
      ReminderUnit reminderUnit) {
    this.title = title;
    this.scheduleType = scheduleType;
    this.eventDate = eventDate;
    this.eventTime = eventTime;
    this.location = location;
    this.memo = memo;
    this.reminderEnabled = reminderEnabled;
    this.reminderValue = reminderValue;
    this.reminderUnit = reminderUnit;
    // 수정 시 리마인더를 재무장한다 — 그대로 두면 날짜 변경 후에도 과거 발송 기록 때문에 새 알림이 스킵된다.
    this.reminderSentAt = null;
  }

  public void markReminderSent() {
    this.reminderSentAt = LocalDateTime.now();
  }
}
