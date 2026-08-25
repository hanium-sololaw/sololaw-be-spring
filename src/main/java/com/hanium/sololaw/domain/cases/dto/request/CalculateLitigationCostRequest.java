/* 
 * Copyright (c) HANIUM SOLOLAW 
 */
package com.hanium.sololaw.domain.cases.dto.request;

import java.math.BigDecimal;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import com.hanium.sololaw.domain.cases.entity.enums.FilingMethod;
import com.hanium.sololaw.domain.cases.entity.enums.LitigationInstance;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "소송비용(인지대·송달료) 계산 요청 DTO — 특정 사건과 무관하게 입력값만으로 계산한다")
public record CalculateLitigationCostRequest(
    @NotNull @Positive @Schema(description = "청구 금액(소송목적의 값)", example = "10000000")
        BigDecimal claimAmount,
    @NotNull @Min(1) @Schema(description = "원고 수", example = "1") Integer plaintiffCount,
    @NotNull @Min(1) @Schema(description = "피고 수", example = "1") Integer defendantCount,
    @NotNull @Schema(description = "제출 방법") FilingMethod filingMethod,
    @NotNull @Schema(description = "심급") LitigationInstance instance) {}
