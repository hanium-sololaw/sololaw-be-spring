/* 
 * Copyright (c) HANIUM SOLOLAW 
 */
package com.hanium.sololaw.domain.document.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import tools.jackson.databind.JsonNode;

@Schema(description = "문서 초안 수정 요청 DTO (null인 필드는 변경하지 않음)")
public record UpdateDocumentRequest(
    @Schema(description = "문서/파일명") String title,
    @Schema(description = "위저드 폼 입력값") JsonNode content,
    @Schema(description = "작성률 %(0~100)") Integer writingRate) {}
