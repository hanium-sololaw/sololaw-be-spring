/* 
 * Copyright (c) HANIUM SOLOLAW 
 */
package com.hanium.sololaw.domain.precedentsubscription.mapper;

import org.springframework.stereotype.Component;

import com.hanium.sololaw.domain.precedentsubscription.dto.response.PrecedentSubscriptionResponse;
import com.hanium.sololaw.domain.precedentsubscription.entity.PrecedentSubscription;

@Component
public class PrecedentSubscriptionMapper {

  /**
   * @param precedentSubscription : 변환할 PrecedentSubscription Entity
   */
  public PrecedentSubscriptionResponse toResponse(PrecedentSubscription precedentSubscription) {
    return PrecedentSubscriptionResponse.builder()
        .plan(precedentSubscription.getPlan())
        .status(precedentSubscription.getStatus())
        .priceKrw(precedentSubscription.getPriceKrw())
        .billingCycle(precedentSubscription.getBillingCycle())
        .nextBillingAt(precedentSubscription.getNextBillingAt())
        .canceledAt(precedentSubscription.getCanceledAt())
        .startedAt(precedentSubscription.getStartedAt())
        .expiresAt(precedentSubscription.getExpiresAt())
        .build();
  }
}
