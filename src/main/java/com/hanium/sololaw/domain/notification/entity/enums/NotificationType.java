/* 
 * Copyright (c) HANIUM SOLOLAW 
 */
package com.hanium.sololaw.domain.notification.entity.enums;

import com.hanium.sololaw.domain.schedule.entity.enums.ScheduleType;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
@Schema(description = "알림 유형")
public enum NotificationType {
  DEADLINE("기한"),
  HEARING("기일"),
  OPPONENT_FILING("상대방제출"),
  AI_PRECEDENT("AI판례발견"),
  EVIDENCE_SUPPLEMENT("증거보완");

  private final String description;

  /** 일정 리마인더 알림을 만들 때 쓰는 매핑. HEARING만 기일 알림이고 나머지 전체는 기한 알림으로 묶는다. */
  public static NotificationType fromScheduleType(ScheduleType scheduleType) {
    return scheduleType == ScheduleType.HEARING ? HEARING : DEADLINE;
  }
}
