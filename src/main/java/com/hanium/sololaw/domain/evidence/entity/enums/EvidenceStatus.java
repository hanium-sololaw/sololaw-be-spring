/* 
 * Copyright (c) HANIUM SOLOLAW 
 */
package com.hanium.sololaw.domain.evidence.entity.enums;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
@Schema(description = "증거 제출 상태")
public enum EvidenceStatus {
  PENDING("대기중"),
  NOT_SUBMITTED("미제출"),
  SUBMITTED("제출완료"),
  NEEDS_SUPPLEMENT("보완필요");

  private final String description;
}
