/* 
 * Copyright (c) HANIUM SOLOLAW 
 */
package com.hanium.sololaw.domain.precedent.dto.response;

import java.time.LocalDateTime;

import com.hanium.sololaw.domain.precedent.entity.enums.LegalCategory;
import com.hanium.sololaw.domain.precedent.entity.enums.PrecedentOutcome;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
@Schema(description = "판례 저장 응답 DTO")
public record PrecedentBookmarkResponse(
    @Schema(description = "저장 ID") Long id,
    @Schema(description = "RAG 판례일련번호") String serialId,
    @Schema(description = "사건명") String name,
    @Schema(description = "사건번호") String caseNo,
    @Schema(description = "법원") String court,
    @Schema(description = "선고일") String decisionDate,
    @Schema(description = "승패 결과") PrecedentOutcome outcome,
    @Schema(description = "판례 분류") LegalCategory category,
    @Schema(description = "참고 포인트 요약") String referenceNote,
    @Schema(description = "원문보기 링크") String detailUrl,
    @Schema(description = "생성 시각") LocalDateTime createdAt,
    @Schema(description = "수정 시각") LocalDateTime modifiedAt) {}
