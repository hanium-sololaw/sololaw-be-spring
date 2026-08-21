/* 
 * Copyright (c) HANIUM SOLOLAW 
 */
package com.hanium.sololaw.domain.evidence.entity.enums;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
@Schema(description = "호증 당사자(갑/을/병)")
public enum ExhibitParty {
  GAP("갑(원고호증)"),
  EUL("을(피고호증)"),
  BYEONG("병(제3자호증)");

  private final String description;
}
