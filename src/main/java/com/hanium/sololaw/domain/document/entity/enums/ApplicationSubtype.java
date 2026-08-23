/* 
 * Copyright (c) HANIUM SOLOLAW 
 */
package com.hanium.sololaw.domain.document.entity.enums;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
@Schema(
    description = "신청서 하위 유형(docType=APPLICATION일 때만 사용). AI 서버가 실제로 생성 가능한 5종 기준(2026-08-23 정정)")
public enum ApplicationSubtype {
  PAYMENT_ORDER("지급명령신청서"),
  LITIGATION_AID("소송구조신청서"),
  LEASE_REGISTRATION("임차권등기명령신청서"),
  ENFORCEMENT("강제집행신청서"),
  PROVISIONAL_SEIZURE("가압류신청서");

  private final String description;
}
