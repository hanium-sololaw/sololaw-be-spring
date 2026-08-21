/* 
 * Copyright (c) HANIUM SOLOLAW 
 */
package com.hanium.sololaw.domain.schedule.mapper;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

import org.springframework.stereotype.Component;

import com.hanium.sololaw.domain.schedule.dto.request.CreateScheduleRequest;
import com.hanium.sololaw.domain.schedule.dto.response.ScheduleResponse;
import com.hanium.sololaw.domain.schedule.entity.Schedule;

@Component
public class ScheduleMapper {

  /**
   * @param userId : 소유자 사용자 ID
   * @param request : 변환할 CreateScheduleRequest
   * @return : 변환된 Schedule Entity
   */
  public Schedule toEntity(Long userId, CreateScheduleRequest request) {
    return Schedule.builder()
        .userId(userId)
        .caseId(request.caseId())
        .title(request.title())
        .scheduleType(request.scheduleType())
        .eventDate(request.eventDate())
        .eventTime(request.eventTime())
        .location(request.location())
        .memo(request.memo())
        .reminderEnabled(request.reminderEnabled() != null ? request.reminderEnabled() : false)
        .reminderValue(request.reminderValue())
        .reminderUnit(request.reminderUnit())
        .build();
  }

  /**
   * @param schedule : 변환할 Schedule Entity
   */
  public ScheduleResponse toResponse(Schedule schedule) {
    return ScheduleResponse.builder()
        .id(schedule.getId())
        .caseId(schedule.getCaseId())
        .title(schedule.getTitle())
        .scheduleType(schedule.getScheduleType())
        .eventDate(schedule.getEventDate())
        .eventTime(schedule.getEventTime())
        .location(schedule.getLocation())
        .memo(schedule.getMemo())
        .reminderEnabled(schedule.getReminderEnabled())
        .reminderValue(schedule.getReminderValue())
        .reminderUnit(schedule.getReminderUnit())
        .dDay(ChronoUnit.DAYS.between(LocalDate.now(), schedule.getEventDate()))
        .createdAt(schedule.getCreatedAt())
        .modifiedAt(schedule.getModifiedAt())
        .build();
  }

  /**
   * @param schedules : 변환할 Schedule Entity 목록
   */
  public List<ScheduleResponse> toResponseList(List<Schedule> schedules) {
    return schedules.stream().map(this::toResponse).toList();
  }
}
