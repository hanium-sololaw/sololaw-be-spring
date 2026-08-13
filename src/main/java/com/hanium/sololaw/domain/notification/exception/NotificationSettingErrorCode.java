/* 
 * Copyright (c) HANIUM SOLOLAW 
 */
package com.hanium.sololaw.domain.notification.exception;

import org.springframework.http.HttpStatus;

import com.hanium.sololaw.global.exception.model.BaseErrorCode;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum NotificationSettingErrorCode implements BaseErrorCode {
  NOTIFICATION_SETTING_NOT_FOUND("NOTI4001", "알림 설정을 찾을 수 없습니다.", HttpStatus.NOT_FOUND);

  private final String code;
  private final String message;
  private final HttpStatus status;
}
