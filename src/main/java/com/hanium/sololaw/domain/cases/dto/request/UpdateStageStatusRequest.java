/* 
 * Copyright (c) HANIUM SOLOLAW 
 */
package com.hanium.sololaw.domain.cases.dto.request;

import jakarta.validation.constraints.NotNull;

import com.hanium.sololaw.domain.cases.entity.enums.StageStatus;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "절차 단계 상태 변경 요청 DTO")
public record UpdateStageStatusRequest(
    @NotNull @Schema(description = "변경할 단계 상태") StageStatus status) {}
