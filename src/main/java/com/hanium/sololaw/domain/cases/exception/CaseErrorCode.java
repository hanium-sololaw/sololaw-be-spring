/* 
 * Copyright (c) HANIUM SOLOLAW 
 */
package com.hanium.sololaw.domain.cases.exception;

import org.springframework.http.HttpStatus;

import com.hanium.sololaw.global.exception.model.BaseErrorCode;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum CaseErrorCode implements BaseErrorCode {
  CASE_NOT_FOUND("CASE4001", "사건 정보를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
  CASE_PARTY_NOT_FOUND("CASE4002", "당사자 정보를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
  LITIGATION_STAGE_NOT_FOUND("CASE4003", "절차 단계 정보를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
  CASE_TODO_NOT_FOUND("CASE4004", "할 일 정보를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
  CLAIM_AMOUNT_REQUIRED("CASE4005", "청구금액이 등록되지 않아 소송비용을 계산할 수 없습니다.", HttpStatus.BAD_REQUEST),
  DUPLICATE_STAGE_ORDER("CASE4091", "이미 절차 단계가 생성된 사건입니다.", HttpStatus.CONFLICT);

  private final String code;
  private final String message;
  private final HttpStatus status;
}
