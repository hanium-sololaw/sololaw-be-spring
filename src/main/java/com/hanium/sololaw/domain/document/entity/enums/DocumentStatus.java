/* 
 * Copyright (c) HANIUM SOLOLAW 
 */
package com.hanium.sololaw.domain.document.entity.enums;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
@Schema(description = "문서 제출 상태(4단계, 사용자가 수동으로 표시)")
public enum DocumentStatus {
  DRAFT("작성 중"),
  SCHEDULED_TO_SUBMIT("제출예정"),
  SUBMITTED("제출완료"),
  NEEDS_REVISION("보완필요");

  private final String description;
}
