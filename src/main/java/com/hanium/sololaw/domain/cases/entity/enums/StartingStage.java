/* 
 * Copyright (c) HANIUM SOLOLAW 
 */
package com.hanium.sololaw.domain.cases.entity.enums;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/** 사건 생성 시 "어디까지 진행됐나요?" 선택값. litigation_stages에 컬럼으로 저장하지 않고, 6단계 자동 시드 시점에만 사용하는 요청 전용 값이다. */
@Getter
@RequiredArgsConstructor
@Schema(description = "사건 생성 시 시작 지점(시드 시점 파라미터, 컬럼 아님)")
public enum StartingStage {
  DISPUTE("분쟁이 막 생겼어요"),
  DEMAND_LETTER("내용증명까지 보냈어요"),
  COMPLAINT_DRAFT("소장부터 준비하려고요"),
  COURT_FILED("이미 소장을 냈어요");

  private final String description;
}
