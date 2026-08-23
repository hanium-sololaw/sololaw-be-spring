/* 
 * Copyright (c) HANIUM SOLOLAW 
 */
package com.hanium.sololaw.domain.precedent.dto.request;

import jakarta.validation.constraints.NotNull;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "인용을 문서에 연결하는 요청 DTO")
public record LinkDocumentRequest(@NotNull @Schema(description = "연결할 문서 ID") Long documentId) {}
