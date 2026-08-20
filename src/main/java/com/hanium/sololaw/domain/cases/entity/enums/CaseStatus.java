/* 
 * Copyright (c) HANIUM SOLOLAW 
 */
package com.hanium.sololaw.domain.cases.entity.enums;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
@Schema(description = "사건 상태(사건상세 \"상태 정정\" 모달 드롭다운 5단계)")
public enum CaseStatus {
  PREPARING("작성 중"),
  SUBMISSION_READY("제출 준비"),
  FILED("접수함"),
  IN_PROGRESS("진행 중"),
  CLOSED("종결");

  private final String description;
}
