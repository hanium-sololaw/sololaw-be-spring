/* 
 * Copyright (c) HANIUM SOLOLAW 
 */
package com.hanium.sololaw.domain.precedent.exception;

import org.springframework.http.HttpStatus;

import com.hanium.sololaw.global.exception.model.BaseErrorCode;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PrecedentErrorCode implements BaseErrorCode {
  PRECEDENT_BOOKMARK_NOT_FOUND("PREC4001", "저장된 판례를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
  PRECEDENT_CITATION_NOT_FOUND("PREC4002", "인용 정보를 찾을 수 없습니다.", HttpStatus.NOT_FOUND);

  private final String code;
  private final String message;
  private final HttpStatus status;
}
