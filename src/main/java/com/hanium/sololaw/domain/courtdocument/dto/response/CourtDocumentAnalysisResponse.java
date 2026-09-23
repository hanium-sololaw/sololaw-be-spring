/* 
 * Copyright (c) HANIUM SOLOLAW 
 */
package com.hanium.sololaw.domain.courtdocument.dto.response;

import java.time.LocalDate;
import java.time.LocalTime;

import com.hanium.sololaw.domain.schedule.entity.enums.ScheduleType;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
@Schema(description = "법원 문서 분석 결과 DTO")
public record CourtDocumentAnalysisResponse(
    @Schema(description = "분석 성공 여부") boolean success,
    @Schema(description = "success=false일 때 사유") String message,
    @Schema(description = "인식된 문서 종류(기일통지서/보정명령/소장부본송달 등 원문 표기)") String documentType,
    @Schema(description = "추출된 사건번호") String caseNumber,
    @Schema(description = "추출된 사건명") String caseName,
    @Schema(description = "사건번호로 매칭된 내 사건 ID(매칭 실패 시 null)") Long matchedCaseId,
    @Schema(description = "일정 생성 시 그대로 사용할 수 있는 일정 유형") ScheduleType scheduleType,
    @Schema(description = "변론기일 날짜(기일통지서인 경우)") LocalDate hearingDate,
    @Schema(description = "변론기일 시각(기일통지서인 경우)") LocalTime hearingTime,
    @Schema(description = "관할 법원") String court,
    @Schema(description = "재판부") String division,
    @Schema(description = "제출 기한(보정명령·소장부본송달 등인 경우)") LocalDate submissionDeadline,
    @Schema(description = "제출 서류 종류(보정서/답변서/준비서면 등)") String deadlineType) {}
