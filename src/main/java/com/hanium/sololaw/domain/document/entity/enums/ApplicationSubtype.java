/* 
 * Copyright (c) HANIUM SOLOLAW 
 */
package com.hanium.sololaw.domain.document.entity.enums;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
@Schema(description = "신청서 하위 유형(docType=APPLICATION일 때만 사용)")
public enum ApplicationSubtype {
  DATE_CHANGE("기일변경신청서"),
  DOC_DISPATCH("문서송부촉탁신청서"),
  CORRECTION("보정서"),
  LITIGATION_AID("소송구조신청서");

  private final String description;
}
