/* 
 * Copyright (c) HANIUM SOLOLAW 
 */
package com.hanium.sololaw.domain.document.dto.request;

import jakarta.validation.constraints.NotNull;

import com.hanium.sololaw.domain.document.entity.enums.ApplicationSubtype;
import com.hanium.sololaw.domain.document.entity.enums.DocType;

import io.swagger.v3.oas.annotations.media.Schema;
import tools.jackson.databind.JsonNode;

@Schema(description = "문서 초안 생성 요청 DTO")
public record CreateDocumentRequest(
    @NotNull @Schema(description = "문서 유형") DocType docType,
    @Schema(description = "신청서 하위 유형(docType=APPLICATION일 때만)")
        ApplicationSubtype applicationSubtype,
    @Schema(description = "문서/파일명") String title,
    @Schema(description = "위저드 폼 입력값(입력 전용, 필드 구조는 프론트가 결정)") JsonNode content) {}
