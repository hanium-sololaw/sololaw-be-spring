/* 
 * Copyright (c) HANIUM SOLOLAW 
 */
package com.hanium.sololaw.domain.precedent.entity.enums;

import com.fasterxml.jackson.annotation.JsonCreator;

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

  /**
   * RAG 판례 검색 응답이 소문자로 내려주는 값("win" 등)을 그대로 받아들인다. 대문자 상수명("WIN")도 그대로 계속 허용한다 — 프론트가 "RAG 응답을 그대로
   * 전달"하도록 만든 DTO 주석이 실제로 동작하게 하기 위함.
   */
  @JsonCreator
  public static PrecedentOutcome from(String value) {
    for (PrecedentOutcome outcome : values()) {
      if (outcome.name().equalsIgnoreCase(value)) {
        return outcome;
      }
    }
    throw new IllegalArgumentException("알 수 없는 판례 결과 값: " + value);
  }
}
