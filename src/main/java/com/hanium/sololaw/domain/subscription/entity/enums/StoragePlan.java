/* 
 * Copyright (c) HANIUM SOLOLAW 
 */
package com.hanium.sololaw.domain.subscription.entity.enums;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
@Schema(description = "저장공간 구독 플랜")
public enum StoragePlan {
  FREE("무료, 500MB", 0, 524_288_000L),
  STANDARD("안심 보관, 10GB · ₩12,900/월", 12_900, 10L * 1024 * 1024 * 1024),
  PRO("전문 보관, 50GB · ₩24,900/월", 24_900, 50L * 1024 * 1024 * 1024);

  private final String description;
  private final int priceKrw;
  private final long storageLimitBytes;
}
