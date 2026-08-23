/* 
 * Copyright (c) HANIUM SOLOLAW 
 */
package com.hanium.sololaw.domain.precedent.dto.request;

import jakarta.validation.constraints.NotBlank;

import com.hanium.sololaw.domain.precedent.entity.enums.LegalCategory;
import com.hanium.sololaw.domain.precedent.entity.enums.PrecedentOutcome;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "판례 저장 요청 DTO(프론트가 RAG 검색 응답을 그대로 전달)")
public record CreatePrecedentBookmarkRequest(
    @NotBlank @Schema(description = "RAG 판례일련번호") String serialId,
    @NotBlank @Schema(description = "사건명") String name,
    @NotBlank @Schema(description = "사건번호", example = "2022다123456") String caseNo,
    @Schema(description = "법원") String court,
    @Schema(description = "선고일(RAG 응답 원본 문자열)") String decisionDate,
    @Schema(description = "승패 결과(판정 불가 시 UNKNOWN 또는 미전달)") PrecedentOutcome outcome,
    @Schema(description = "판례 분류") LegalCategory category,
    @Schema(description = "참고 포인트 요약") String referenceNote,
    @Schema(description = "원문보기 링크") String detailUrl) {}
