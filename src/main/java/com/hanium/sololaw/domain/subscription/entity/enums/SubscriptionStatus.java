/* 
 * Copyright (c) HANIUM SOLOLAW 
 */
package com.hanium.sololaw.domain.subscription.entity.enums;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
@Schema(description = "구독 상태")
public enum SubscriptionStatus {
  ACTIVE("이용중"),
  CANCELED("해지"),
  EXPIRED("만료");

  private final String description;
}
