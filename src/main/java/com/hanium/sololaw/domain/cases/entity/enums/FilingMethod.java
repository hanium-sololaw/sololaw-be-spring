/* 
 * Copyright (c) HANIUM SOLOLAW 
 */
package com.hanium.sololaw.domain.cases.entity.enums;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
@Schema(description = "법원 접수 방법(사건상세 법원 접수 정보 카드)")
public enum FilingMethod {
  ELECTRONIC("전자소송"),
  PAPER("종이 제출");

  private final String description;
}
