/* 
 * Copyright (c) HANIUM SOLOLAW 
 */
package com.hanium.sololaw.domain.notification.entity.enums;

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
}
