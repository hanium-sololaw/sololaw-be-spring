/* 
 * Copyright (c) HANIUM SOLOLAW 
 */
package com.hanium.sololaw.domain.precedentsubscription.exception;

import org.springframework.http.HttpStatus;

import com.hanium.sololaw.global.exception.model.BaseErrorCode;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PrecedentSubscriptionErrorCode implements BaseErrorCode {
  PRECEDENT_SUBSCRIPTION_NOT_FOUND("PSUB4001", "판례검색 구독 정보를 찾을 수 없습니다.", HttpStatus.NOT_FOUND);

  private final String code;
  private final String message;
  private final HttpStatus status;
}
