/* 
 * Copyright (c) HANIUM SOLOLAW 
 */
package com.hanium.sololaw.domain.subscription.entity.enums;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
@Schema(description = "구독 플랜")
public enum SubscriptionPlan {
  FREE("무료(100MB)"),
  PREMIUM("프리미엄(30GB, ₩9,900/월)");

  private final String description;
}
