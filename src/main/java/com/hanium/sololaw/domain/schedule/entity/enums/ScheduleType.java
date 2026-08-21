/* 
 * Copyright (c) HANIUM SOLOLAW 
 */
package com.hanium.sololaw.domain.schedule.entity.enums;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
@Schema(description = "일정 유형")
public enum ScheduleType {
  SUBMISSION_DEADLINE("제출기한"),
  HEARING("변론기일"),
  PREPARATION("준비기간"),
  ATTENDANCE("출석");

  private final String description;
}
