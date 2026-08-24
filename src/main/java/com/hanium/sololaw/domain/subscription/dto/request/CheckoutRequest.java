/* 
 * Copyright (c) HANIUM SOLOLAW 
 */
package com.hanium.sololaw.domain.subscription.dto.request;

import jakarta.validation.constraints.NotNull;

import com.hanium.sololaw.domain.subscription.entity.enums.StoragePlan;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "결제 시작 요청 DTO")
public record CheckoutRequest(@NotNull @Schema(description = "구독할 플랜(FREE 불가)") StoragePlan plan) {}
