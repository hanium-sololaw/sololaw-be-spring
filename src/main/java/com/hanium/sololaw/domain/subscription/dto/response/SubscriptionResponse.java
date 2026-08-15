/* 
 * Copyright (c) HANIUM SOLOLAW 
 */
package com.hanium.sololaw.domain.subscription.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.hanium.sololaw.domain.subscription.entity.enums.BillingCycle;
import com.hanium.sololaw.domain.subscription.entity.enums.StoragePlan;
import com.hanium.sololaw.domain.subscription.entity.enums.SubscriptionStatus;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
@Schema(description = "내 구독 응답 DTO")
public record SubscriptionResponse(
    @Schema(description = "저장공간 구독 플랜") StoragePlan plan,
    @Schema(description = "구독 상태") SubscriptionStatus status,
    @Schema(description = "저장 용량 한도(바이트)", example = "524288000") Long storageLimitBytes,
    @Schema(description = "현재 사용 중인 저장 용량(바이트)", example = "0") Long usedStorageBytes,
    @Schema(description = "월 구독료(원)", example = "0") BigDecimal priceKrw,
    @Schema(description = "결제 주기") BillingCycle billingCycle,
    @Schema(description = "다음 결제 예정일") LocalDateTime nextBillingAt,
    @Schema(description = "해지 요청 시각") LocalDateTime canceledAt,
    @Schema(description = "구독 시작 시각") LocalDateTime startedAt,
    @Schema(description = "구독 만료 시각") LocalDateTime expiresAt) {}
