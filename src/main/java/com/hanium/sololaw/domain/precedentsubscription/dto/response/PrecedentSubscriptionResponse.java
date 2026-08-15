/* 
 * Copyright (c) HANIUM SOLOLAW 
 */
package com.hanium.sololaw.domain.precedentsubscription.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.hanium.sololaw.domain.precedentsubscription.entity.enums.PrecedentSearchPlan;
import com.hanium.sololaw.domain.subscription.entity.enums.BillingCycle;
import com.hanium.sololaw.domain.subscription.entity.enums.SubscriptionStatus;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
@Schema(description = "내 판례검색 구독 응답 DTO")
public record PrecedentSubscriptionResponse(
    @Schema(description = "판례검색 구독 플랜") PrecedentSearchPlan plan,
    @Schema(description = "구독 상태") SubscriptionStatus status,
    @Schema(description = "월 구독료(원)", example = "0") BigDecimal priceKrw,
    @Schema(description = "결제 주기") BillingCycle billingCycle,
    @Schema(description = "다음 결제 예정일") LocalDateTime nextBillingAt,
    @Schema(description = "해지 요청 시각") LocalDateTime canceledAt,
    @Schema(description = "구독 시작 시각") LocalDateTime startedAt,
    @Schema(description = "구독 만료 시각") LocalDateTime expiresAt) {}
