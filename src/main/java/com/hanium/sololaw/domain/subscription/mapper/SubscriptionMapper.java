/* 
 * Copyright (c) HANIUM SOLOLAW 
 */
package com.hanium.sololaw.domain.subscription.mapper;

import org.springframework.stereotype.Component;

import com.hanium.sololaw.domain.subscription.dto.response.SubscriptionResponse;
import com.hanium.sololaw.domain.subscription.entity.Subscription;

@Component
public class SubscriptionMapper {

  /**
   * @param subscription : 변환할 Subscription Entity
   */
  public SubscriptionResponse toResponse(Subscription subscription) {
    return SubscriptionResponse.builder()
        .plan(subscription.getPlan())
        .status(subscription.getStatus())
        .storageLimitBytes(subscription.getStorageLimitBytes())
        .usedStorageBytes(subscription.getUsedStorageBytes())
        .priceKrw(subscription.getPriceKrw())
        .billingCycle(subscription.getBillingCycle())
        .nextBillingAt(subscription.getNextBillingAt())
        .canceledAt(subscription.getCanceledAt())
        .startedAt(subscription.getStartedAt())
        .expiresAt(subscription.getExpiresAt())
        .build();
  }
}
