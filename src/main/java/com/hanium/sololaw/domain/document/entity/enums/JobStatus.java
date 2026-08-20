/* 
 * Copyright (c) HANIUM SOLOLAW 
 */
package com.hanium.sololaw.domain.document.entity.enums;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
@Schema(description = "문서 생성 로그 상태(선택 기록)")
public enum JobStatus {
  PENDING("대기"),
  RUNNING("실행중"),
  SUCCEEDED("성공"),
  FAILED("실패"),
  CANCELED("취소");

  private final String description;
}
