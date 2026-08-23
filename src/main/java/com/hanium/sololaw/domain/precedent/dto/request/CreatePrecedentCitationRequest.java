/* 
 * Copyright (c) HANIUM SOLOLAW 
 */
package com.hanium.sololaw.domain.precedent.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import com.hanium.sololaw.domain.precedent.entity.enums.LegalCategory;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "판례 인용 추가 요청 DTO(프론트가 RAG 검색 응답을 그대로 전달)")
public record CreatePrecedentCitationRequest(
    @NotBlank @Schema(description = "RAG 판례일련번호") String serialId,
    @NotBlank @Schema(description = "사건명") String name,
    @Schema(description = "법원") String court,
    @Schema(description = "선고일(RAG 응답 원본 문자열)") String decisionDate,
    @Schema(description = "판례 분류") LegalCategory category,
    @Schema(description = "참고 포인트 요약") String referenceNote,
    @Schema(description = "원문보기 링크") String detailUrl,
    @NotNull @Schema(description = "연결 사건 ID(필수)") Long caseId,
    @Schema(description = "연결 문서 ID(선택, 문서 생성이 확정되면 이후 PATCH로도 연결 가능)") Long documentId) {}
