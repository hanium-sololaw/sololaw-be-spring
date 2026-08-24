/* 
 * Copyright (c) HANIUM SOLOLAW 
 */
package com.hanium.sololaw.domain.subscription.exception;

import org.springframework.http.HttpStatus;

import com.hanium.sololaw.global.exception.model.BaseErrorCode;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum SubscriptionErrorCode implements BaseErrorCode {
  SUBSCRIPTION_NOT_FOUND("SUB4001", "구독 정보를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
  PAYMENT_GATEWAY_ERROR("SUB4002", "결제 처리 중 오류가 발생했습니다.", HttpStatus.BAD_GATEWAY),
  INVALID_CHECKOUT_SESSION("SUB4003", "유효하지 않거나 만료된 결제 요청입니다.", HttpStatus.BAD_REQUEST),
  PAYMENT_AMOUNT_MISMATCH("SUB4004", "결제 금액이 일치하지 않습니다.", HttpStatus.BAD_REQUEST),
  ALREADY_SUBSCRIBED_PLAN("SUB4005", "이미 해당 플랜을 구독 중입니다.", HttpStatus.CONFLICT),
  FREE_PLAN_NO_PAYMENT_REQUIRED("SUB4006", "무료 플랜은 결제가 필요하지 않습니다.", HttpStatus.BAD_REQUEST),
  NO_ACTIVE_PAID_SUBSCRIPTION("SUB4007", "해지할 활성 구독이 없습니다.", HttpStatus.NOT_FOUND);

  private final String code;
  private final String message;
  private final HttpStatus status;
}
