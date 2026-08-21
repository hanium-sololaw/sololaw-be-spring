/* 
 * Copyright (c) HANIUM SOLOLAW 
 */
package com.hanium.sololaw.domain.schedule.dto.request;

import java.time.LocalDate;
import java.time.LocalTime;

import com.hanium.sololaw.domain.schedule.entity.enums.ReminderUnit;
import com.hanium.sololaw.domain.schedule.entity.enums.ScheduleType;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "일정 수정 요청 DTO (null인 필드는 변경하지 않음)")
public record UpdateScheduleRequest(
    @Schema(description = "일정 제목") String title,
    @Schema(description = "일정 유형") ScheduleType scheduleType,
    @Schema(description = "일자") LocalDate eventDate,
    @Schema(description = "시각") LocalTime eventTime,
    @Schema(description = "장소") String location,
    @Schema(description = "메모") String memo,
    @Schema(description = "개별 리마인더 사용 여부") Boolean reminderEnabled,
    @Schema(description = "리마인더 값") Integer reminderValue,
    @Schema(description = "리마인더 단위") ReminderUnit reminderUnit) {}
