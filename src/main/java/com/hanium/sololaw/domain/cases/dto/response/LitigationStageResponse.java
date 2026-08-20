/* 
 * Copyright (c) HANIUM SOLOLAW 
 */
package com.hanium.sololaw.domain.cases.dto.response;

import java.time.LocalDate;

import com.hanium.sololaw.domain.cases.entity.enums.StageStatus;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
@Schema(description = "절차 단계 응답 DTO")
public record LitigationStageResponse(
    @Schema(description = "단계 ID") Long id,
    @Schema(description = "단계 순서(1~6)") Integer stageOrder,
    @Schema(description = "단계명") String name,
    @Schema(description = "단계 상태") StageStatus status,
    @Schema(description = "단계 일자") LocalDate stageDate,
    @Schema(description = "설명") String description) {}
