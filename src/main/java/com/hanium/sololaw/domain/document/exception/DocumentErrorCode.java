/* 
 * Copyright (c) HANIUM SOLOLAW 
 */
package com.hanium.sololaw.domain.document.exception;

import org.springframework.http.HttpStatus;

import com.hanium.sololaw.global.exception.model.BaseErrorCode;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum DocumentErrorCode implements BaseErrorCode {
  DOCUMENT_NOT_FOUND("DOC4001", "문서 정보를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
  GENERATION_JOB_NOT_FOUND("DOC4002", "생성 로그를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
  DOCUMENT_FILE_NOT_AVAILABLE("DOC4003", "아직 생성되지 않은 문서는 다운로드할 수 없습니다.", HttpStatus.NOT_FOUND),
  DOCUMENT_LOCKED("DOC4091", "생성이 진행 중인 문서는 수정할 수 없습니다.", HttpStatus.CONFLICT);

  private final String code;
  private final String message;
  private final HttpStatus status;
}
