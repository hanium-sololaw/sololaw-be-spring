/* 
 * Copyright (c) HANIUM SOLOLAW 
 */
package com.hanium.sololaw.domain.payment.entity.enums;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/** Payment·결제 대기 세션이 어느 구독 도메인 결제인지 구분한다(저장공간·판례검색은 완전히 독립된 구독이다). */
@Getter
@RequiredArgsConstructor
@Schema(description = "구독 종류")
public enum SubscriptionType {
  STORAGE("저장공간 구독"),
  PRECEDENT_SEARCH("판례검색 구독");

  private final String description;
}
