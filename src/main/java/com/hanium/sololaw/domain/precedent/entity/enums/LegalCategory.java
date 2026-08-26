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
@Schema(description = "판례 분류(RAG 응답 스냅샷 저장용)")
public enum LegalCategory {
  CIVIL("민사"),
  CRIMINAL("형사"),
  ADMIN("행정"),
  FAMILY("가사");

  private final String description;

  /**
   * RAG 판례 검색 응답이 원문 그대로 내려주는 한글 분류값("민사" 등)을 그대로 받아들인다. 대문자 상수명("CIVIL")도 그대로 계속 허용한다 — 프론트가 "RAG
   * 응답을 그대로 전달"하도록 만든 DTO 주석이 실제로 동작하게 하기 위함.
   */
  @JsonCreator
  public static LegalCategory from(String value) {
    for (LegalCategory category : values()) {
      if (category.name().equalsIgnoreCase(value) || category.description.equals(value)) {
        return category;
      }
    }
    throw new IllegalArgumentException("알 수 없는 판례 분류 값: " + value);
  }
}
