/* 
 * Copyright (c) HANIUM SOLOLAW 
 */
package com.hanium.sololaw.domain.cases.entity.enums;

import java.math.BigDecimal;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/** 심급. 인지액 배율은 민사소송 등 인지법 제3조(항소장 1.5배, 상고장 2배) 기준. */
@Getter
@RequiredArgsConstructor
@Schema(description = "심급(인지대·송달료 산출용)")
public enum LitigationInstance {
  FIRST("1심(소장)", BigDecimal.ONE),
  APPEAL("항소(항소장)", BigDecimal.valueOf(1.5)),
  SUPREME("상고(상고장)", BigDecimal.valueOf(2.0));

  private final String description;
  private final BigDecimal stampFeeMultiplier;
}
