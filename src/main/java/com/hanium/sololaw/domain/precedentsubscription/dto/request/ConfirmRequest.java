/* 
 * Copyright (c) HANIUM SOLOLAW 
 */
package com.hanium.sololaw.domain.precedentsubscription.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "결제 승인 요청 DTO")
public record ConfirmRequest(
    @NotBlank @Schema(description = "토스 결제 키") String paymentKey,
    @NotBlank @Schema(description = "체크아웃에서 발급받은 주문 ID") String orderId,
    @NotNull @Schema(description = "결제 금액(원)", example = "14900") Long amount) {}
