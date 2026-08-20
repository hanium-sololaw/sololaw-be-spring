/* 
 * Copyright (c) HANIUM SOLOLAW 
 */
package com.hanium.sololaw.domain.document.dto.request;

import jakarta.validation.constraints.NotNull;

import com.hanium.sololaw.domain.document.entity.enums.DocumentStatus;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "문서 제출 상태 변경 요청 DTO")
public record UpdateDocumentStatusRequest(
    @NotNull @Schema(description = "변경할 상태") DocumentStatus status) {}
