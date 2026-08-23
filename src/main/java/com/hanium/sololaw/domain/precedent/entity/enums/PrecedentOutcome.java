/* 
 * Copyright (c) HANIUM SOLOLAW 
 */
package com.hanium.sololaw.domain.precedent.entity.enums;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
@Schema(description = "판례 승패 결과(AI 응답 스냅샷 저장용, 판정 불가 시 UNKNOWN)")
public enum PrecedentOutcome {
  WIN("승"),
  PARTIAL("일부승"),
  LOSE("패"),
  UNKNOWN("판정불가");

  private final String description;
}
