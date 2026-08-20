/* 
 * Copyright (c) HANIUM SOLOLAW 
 */
package com.hanium.sololaw.domain.cases.entity.enums;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
@Schema(description = "사건 유형(청구 유형). 나홀로소송 특화 5종, 미정은 NULL")
public enum CaseType {
  LOAN("대여금 반환"),
  DEPOSIT("임대차보증금 반환"),
  WAGE("임금체불청구"),
  TORT("손해 배상"),
  EVICTION("건물명도");

  private final String description;
}
