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
  FREE("무료, 500MB"),
  STANDARD("안심 보관, 10GB · ₩12,900/월"),
  PRO("전문 보관, 50GB · ₩24,900/월");

  private final String description;
}
