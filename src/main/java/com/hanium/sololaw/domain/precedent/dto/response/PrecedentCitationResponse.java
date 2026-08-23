/* 
 * Copyright (c) HANIUM SOLOLAW 
 */
package com.hanium.sololaw.domain.precedent.dto.response;

import java.time.LocalDateTime;

import com.hanium.sololaw.domain.precedent.entity.enums.LegalCategory;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
@Schema(description = "판례 인용 응답 DTO")
public record PrecedentCitationResponse(
    @Schema(description = "인용 ID") Long id,
    @Schema(description = "RAG 판례일련번호") String serialId,
    @Schema(description = "사건명") String name,
    @Schema(description = "사건번호") String caseNo,
    @Schema(description = "법원") String court,
    @Schema(description = "선고일") String decisionDate,
    @Schema(description = "판례 분류") LegalCategory category,
    @Schema(description = "참고 포인트 요약") String referenceNote,
    @Schema(description = "원문보기 링크") String detailUrl,
    @Schema(description = "연결 사건 ID") Long caseId,
    @Schema(description = "연결 문서 ID(nullable)") Long documentId,
    @Schema(description = "인용 시각") LocalDateTime citedAt,
    @Schema(description = "생성 시각") LocalDateTime createdAt,
    @Schema(description = "수정 시각") LocalDateTime modifiedAt) {}
