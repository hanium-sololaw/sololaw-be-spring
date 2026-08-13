/* 
 * Copyright (c) HANIUM SOLOLAW 
 */
package com.hanium.sololaw.domain.user.exception;

import org.springframework.http.HttpStatus;

import com.hanium.sololaw.global.exception.model.BaseErrorCode;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum UserErrorCode implements BaseErrorCode {
  USER_NOT_FOUND("USER4001", "사용자를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
  CURRENT_PASSWORD_MISMATCH("USER4002", "현재 비밀번호가 일치하지 않습니다.", HttpStatus.BAD_REQUEST),
  WITHDRAW_PASSWORD_MISMATCH("USER4003", "비밀번호가 일치하지 않습니다.", HttpStatus.BAD_REQUEST),
  ALREADY_EXIST_EMAIL("USER4004", "이미 사용 중인 이메일입니다.", HttpStatus.BAD_REQUEST);

  private final String code;
  private final String message;
  private final HttpStatus status;
}
