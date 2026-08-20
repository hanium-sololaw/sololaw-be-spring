/* 
 * Copyright (c) HANIUM SOLOLAW 
 */
package com.hanium.sololaw.domain.document.entity.enums;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
@Schema(description = "문서 유형")
public enum DocType {
  COMPLAINT("소장"),
  BRIEF("준비서면"),
  EVIDENCE_LIST("증거목록"),
  APPLICATION("신청서"),
  ANSWER("답변서 — RAG 생성 대상 아님, 참고용 기록");

  private final String description;
}
