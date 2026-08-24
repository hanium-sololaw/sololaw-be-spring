/* 
 * Copyright (c) HANIUM SOLOLAW 
 */
package com.hanium.sololaw.domain.precedentsubscription.entity.enums;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
@Schema(description = "판례검색 구독 플랜")
public enum PrecedentSearchPlan {
  FREE("무료, 검색당 유사판례 최대 5건", 0),
  PREMIUM("전체 검색결과+원문 연속 확인, ₩14,900/월", 14_900);

  private final String description;
  private final int priceKrw;
}
