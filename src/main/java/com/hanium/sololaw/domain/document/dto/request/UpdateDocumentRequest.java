/* 
 * Copyright (c) HANIUM SOLOLAW 
 */
package com.hanium.sololaw.domain.document.dto.request;

import com.fasterxml.jackson.databind.JsonNode;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "문서 초안 수정 요청 DTO (null인 필드는 변경하지 않음)")
public record UpdateDocumentRequest(
    @Schema(description = "문서/파일명") String title,
    @Schema(description = "위저드 폼 입력값") JsonNode content) {}
