/* 
 * Copyright (c) HANIUM SOLOLAW 
 */
package com.hanium.sololaw.domain.evidence.dto.request;

import jakarta.validation.constraints.NotNull;

import com.hanium.sololaw.domain.evidence.entity.enums.EvidenceStatus;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "증거 상태 변경 요청 DTO")
public record UpdateEvidenceStatusRequest(
    @NotNull @Schema(description = "변경할 상태") EvidenceStatus status) {}
