/* 
 * Copyright (c) HANIUM SOLOLAW 
 */
package com.hanium.sololaw.domain.document.dto.response;

import java.time.LocalDateTime;

import com.hanium.sololaw.domain.document.entity.enums.ApplicationSubtype;
import com.hanium.sololaw.domain.document.entity.enums.DocType;
import com.hanium.sololaw.domain.document.entity.enums.DocumentStatus;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import tools.jackson.databind.JsonNode;

@Builder
@Schema(description = "문서 상세 응답 DTO")
public record DocumentDetailResponse(
    @Schema(description = "문서 ID") Long id,
    @Schema(description = "소속 사건 ID") Long caseId,
    @Schema(description = "문서 유형") DocType docType,
    @Schema(description = "신청서 하위 유형") ApplicationSubtype applicationSubtype,
    @Schema(description = "문서/파일명") String title,
    @Schema(description = "제출 상태") DocumentStatus status,
    @Schema(description = "최신본 여부") Boolean isLatest,
    @Schema(description = "작성률 % 0~100") Integer writingRate,
    @Schema(description = "위저드 폼 입력값(입력 전용)") JsonNode content,
    @Schema(description = "AI 생성 본문 구조화") JsonNode generatedContent,
    @Schema(description = "AI 생성 본문 원문") String generatedText,
    @Schema(description = "AI 생성 완료 시각(null이면 미생성)") LocalDateTime generatedAt,
    @Schema(description = "생성 시각") LocalDateTime createdAt,
    @Schema(description = "수정 시각") LocalDateTime modifiedAt) {}
