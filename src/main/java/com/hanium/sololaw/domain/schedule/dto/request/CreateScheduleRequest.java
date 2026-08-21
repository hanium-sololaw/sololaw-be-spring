/* 
 * Copyright (c) HANIUM SOLOLAW 
 */
package com.hanium.sololaw.domain.schedule.dto.request;

import java.time.LocalDate;
import java.time.LocalTime;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import com.hanium.sololaw.domain.schedule.entity.enums.ReminderUnit;
import com.hanium.sololaw.domain.schedule.entity.enums.ScheduleType;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "일정 생성 요청 DTO")
public record CreateScheduleRequest(
    @Schema(description = "연결 사건 ID(선택)") Long caseId,
    @NotBlank @Schema(description = "일정 제목") String title,
    @Schema(description = "일정 유형(선택)") ScheduleType scheduleType,
    @NotNull @Schema(description = "일자") LocalDate eventDate,
    @Schema(description = "시각(선택)") LocalTime eventTime,
    @Schema(description = "장소(선택)") String location,
    @Schema(description = "메모(선택)") String memo,
    @Schema(description = "개별 리마인더 사용 여부(선택, 기본 false)") Boolean reminderEnabled,
    @Schema(description = "리마인더 값(선택, 예: 1)") Integer reminderValue,
    @Schema(description = "리마인더 단위(선택)") ReminderUnit reminderUnit) {}
