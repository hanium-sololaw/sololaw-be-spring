/* 
 * Copyright (c) HANIUM SOLOLAW 
 */
package com.hanium.sololaw.domain.courtdocument.converter;

import java.io.IOException;
import java.util.Base64;

import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import com.hanium.sololaw.domain.courtdocument.exception.CourtDocumentErrorCode;
import com.hanium.sololaw.global.exception.CustomException;

import lombok.extern.slf4j.Slf4j;

/**
 * 업로드된 법원 문서 파일(PDF/JPG/PNG)을 Gemini 입력용 base64 data URI로 변환한다.
 *
 * <p>Gemini는 PDF도 이미지와 동일하게 {@code inlineData}로 그대로 받을 수 있어, 페이지를 이미지로 미리 렌더링할 필요가 없다.
 */
@Slf4j
@Component
public class CourtDocumentFileConverter {

  /**
   * @param file 업로드된 파일(PDF/JPG/PNG — 호출부에서 화이트리스트 검증 완료 가정)
   * @return {@code data:<mime>;base64,...} 형태의 data URI
   */
  public String toDataUri(MultipartFile file) {
    try {
      String encoded = Base64.getEncoder().encodeToString(file.getBytes());
      return "data:%s;base64,%s".formatted(file.getContentType(), encoded);
    } catch (IOException e) {
      log.error("[CourtDocumentFileConverter] toDataUri() - FAIL | error: {}", e.getMessage());
      throw new CustomException(CourtDocumentErrorCode.EXTRACTION_FAILED);
    }
  }
}
