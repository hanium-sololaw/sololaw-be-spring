/* 
 * Copyright (c) HANIUM SOLOLAW 
 */
package com.hanium.sololaw.domain.precedentsubscription.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
@Schema(description = "결제 시작 응답 DTO")
public record CheckoutResponse(
    @Schema(description = "주문 ID") String orderId,
    @Schema(description = "결제 금액(원)") long amount,
    @Schema(description = "주문명") String orderName,
    @Schema(description = "구매자 이메일") String customerEmail,
    @Schema(description = "구매자 이름") String customerName,
    @Schema(description = "토스페이먼츠 SDK 초기화용 클라이언트 키(위젯형 PG만 값이 있음)") String clientKey,
    @Schema(description = "결제 페이지 리다이렉트 URL(리다이렉트형 PG만 값이 있음)") String redirectUrl) {}
