/* 
 * Copyright (c) HANIUM SOLOLAW 
 */
package com.hanium.sololaw.domain.schedule.entity.enums;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
@Schema(description = "일정 유형, 실제 화면 일정 추가 드롭다운 5종은 기일 출석·서류 제출기한·이의불복 기간·준비할 일·기타")
public enum ScheduleType {
  SUBMISSION_DEADLINE("제출기한"),
  HEARING("변론기일"),
  PREPARATION("준비기간"),
  ATTENDANCE("출석"),
  OBJECTION_PERIOD("이의·불복 기간"),
  OTHER("기타");

  private final String description;
}
