/* 
 * Copyright (c) HANIUM SOLOLAW 
 */
package com.hanium.sololaw.domain.evidence.exception;

import org.springframework.http.HttpStatus;

import com.hanium.sololaw.global.exception.model.BaseErrorCode;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum EvidenceErrorCode implements BaseErrorCode {
  EVIDENCE_NOT_FOUND("EVD4001", "증거 정보를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
  EVIDENCE_FOLDER_NOT_FOUND("EVD4002", "증거 폴더 정보를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
  INVALID_FILE_TYPE(
      "EVD4003", "허용되지 않는 파일 형식입니다. (PDF, JPG, PNG, DOCX만 업로드 가능)", HttpStatus.BAD_REQUEST),
  STORAGE_QUOTA_EXCEEDED("EVD4131", "저장 용량이 부족합니다.", HttpStatus.PAYLOAD_TOO_LARGE);

  private final String code;
  private final String message;
  private final HttpStatus status;
}
