/* 
 * Copyright (c) HANIUM SOLOLAW 
 */
package com.hanium.sololaw.domain.precedent.entity.enums;

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
}
