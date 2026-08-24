/* 
 * Copyright (c) HANIUM SOLOLAW 
 */
package com.hanium.sololaw.domain.payment.exception;

import org.springframework.http.HttpStatus;

import com.hanium.sololaw.global.exception.model.BaseErrorCode;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/** PaymentGateway 구현체(TossPaymentGateway 등)가 던지는 PG 연동 오류. 특정 구독 도메인에 속하지 않는 공용 오류다. */
@Getter
@RequiredArgsConstructor
public enum PaymentErrorCode implements BaseErrorCode {
  PAYMENT_GATEWAY_ERROR("PAY5001", "결제 처리 중 오류가 발생했습니다.", HttpStatus.BAD_GATEWAY);

  private final String code;
  private final String message;
  private final HttpStatus status;
}
