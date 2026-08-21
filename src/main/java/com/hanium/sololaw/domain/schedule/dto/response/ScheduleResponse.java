/* 
 * Copyright (c) HANIUM SOLOLAW 
 */
package com.hanium.sololaw.domain.schedule.dto.response;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import com.hanium.sololaw.domain.schedule.entity.enums.ReminderUnit;
import com.hanium.sololaw.domain.schedule.entity.enums.ScheduleType;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
@Schema(description = "일정 응답 DTO")
public record ScheduleResponse(
    @Schema(description = "일정 ID") Long id,
    @Schema(description = "연결 사건 ID(nullable)") Long caseId,
    @Schema(description = "일정 제목") String title,
    @Schema(description = "일정 유형") ScheduleType scheduleType,
    @Schema(description = "일자") LocalDate eventDate,
    @Schema(description = "시각") LocalTime eventTime,
    @Schema(description = "장소") String location,
    @Schema(description = "메모") String memo,
    @Schema(description = "개별 리마인더 사용 여부") Boolean reminderEnabled,
    @Schema(description = "리마인더 값") Integer reminderValue,
    @Schema(description = "리마인더 단위") ReminderUnit reminderUnit,
    @Schema(description = "오늘부터 일정까지 남은 일수(저장값 아닌 응답 시점 파생값, 음수면 지난 일정)") Long dDay,
    @Schema(description = "생성 시각") LocalDateTime createdAt,
    @Schema(description = "수정 시각") LocalDateTime modifiedAt) {}
