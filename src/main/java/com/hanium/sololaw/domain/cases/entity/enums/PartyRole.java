/* 
 * Copyright (c) HANIUM SOLOLAW 
 */
package com.hanium.sololaw.domain.cases.entity.enums;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
@Schema(description = "당사자 역할")
public enum PartyRole {
  PLAINTIFF("원고"),
  DEFENDANT("피고"),
  THIRD_PARTY("제3자");

  private final String description;
}
