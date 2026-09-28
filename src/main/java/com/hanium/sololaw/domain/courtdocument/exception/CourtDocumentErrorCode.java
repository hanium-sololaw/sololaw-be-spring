/* 
 * Copyright (c) HANIUM SOLOLAW 
 */
package com.hanium.sololaw.domain.courtdocument.exception;

import org.springframework.http.HttpStatus;

import com.hanium.sololaw.global.exception.model.BaseErrorCode;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum CourtDocumentErrorCode implements BaseErrorCode {
  INVALID_FILE_TYPE(
      "CDOC4001", "허용되지 않는 파일 형식입니다. (PDF, JPG, PNG만 업로드 가능)", HttpStatus.BAD_REQUEST),
  FILE_TOO_LARGE("CDOC4002", "파일이 10MB를 초과합니다.", HttpStatus.BAD_REQUEST),
  EXTRACTION_FAILED("CDOC5021", "문서 분석에 실패했습니다. 잠시 후 다시 시도해주세요.", HttpStatus.BAD_GATEWAY),
  RATE_LIMITED("CDOC4291", "분석 요청이 너무 잦습니다. 잠시 후 다시 시도해주세요.", HttpStatus.TOO_MANY_REQUESTS);

  private final String code;
  private final String message;
  private final HttpStatus status;
}
