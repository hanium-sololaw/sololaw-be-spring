/* 
 * Copyright (c) HANIUM SOLOLAW 
 */
package com.hanium.sololaw.domain.auth.exception;

import org.springframework.http.HttpStatus;

import com.hanium.sololaw.global.exception.model.BaseErrorCode;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum AuthErrorCode implements BaseErrorCode {
  ALREADY_EXIST_LOGIN_ID("AUTH4001", "이미 동일한 아이디가 존재합니다.", HttpStatus.BAD_REQUEST),
  ALREADY_EXIST_EMAIL("AUTH4002", "이미 동일한 이메일이 존재합니다.", HttpStatus.BAD_REQUEST),
  LOGIN_FAIL("AUTH4003", "아이디 또는 비밀번호가 일치하지 않습니다.", HttpStatus.BAD_REQUEST),
  TERMS_NOT_AGREED("AUTH4004", "서비스 이용약관에 동의해주세요.", HttpStatus.BAD_REQUEST),
  EXPIRED_ACCESS_TOKEN("AUTH4011", "만료된 액세스 토큰입니다.", HttpStatus.UNAUTHORIZED),
  UNAUTHORIZED_TOKEN("AUTH4012", "유효하지 않은 토큰입니다.", HttpStatus.UNAUTHORIZED);

  private final String code;

  private final String message;

  private final HttpStatus status;
}
