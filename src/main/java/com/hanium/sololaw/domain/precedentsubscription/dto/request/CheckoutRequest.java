/* 
 * Copyright (c) HANIUM SOLOLAW 
 */
package com.hanium.sololaw.domain.precedentsubscription.dto.request;

import jakarta.validation.constraints.NotNull;

import com.hanium.sololaw.domain.precedentsubscription.entity.enums.PrecedentSearchPlan;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "결제 시작 요청 DTO")
public record CheckoutRequest(
    @NotNull @Schema(description = "구독할 플랜(FREE 불가)") PrecedentSearchPlan plan) {}
