/* 
 * Copyright (c) HANIUM SOLOLAW 
 */
package com.hanium.sololaw.domain.cases.entity.enums;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
@Schema(description = "절차 단계 상태")
public enum StageStatus {
  SCHEDULED("예정"),
  IN_PROGRESS("진행중"),
  COMPLETED("완료");

  private final String description;
}
