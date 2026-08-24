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
  PRECEDENT_SUBSCRIPTION_NOT_FOUND("PSUB4001", "판례검색 구독 정보를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
  INVALID_CHECKOUT_SESSION("PSUB4002", "유효하지 않거나 만료된 결제 요청입니다.", HttpStatus.BAD_REQUEST),
  PAYMENT_AMOUNT_MISMATCH("PSUB4003", "결제 금액이 일치하지 않습니다.", HttpStatus.BAD_REQUEST),
  ALREADY_SUBSCRIBED_PLAN("PSUB4004", "이미 해당 플랜을 구독 중입니다.", HttpStatus.CONFLICT),
  FREE_PLAN_NO_PAYMENT_REQUIRED("PSUB4005", "무료 플랜은 결제가 필요하지 않습니다.", HttpStatus.BAD_REQUEST),
  NO_ACTIVE_PAID_SUBSCRIPTION("PSUB4006", "해지할 활성 구독이 없습니다.", HttpStatus.NOT_FOUND);

  private final String code;
  private final String message;
  private final HttpStatus status;
}
