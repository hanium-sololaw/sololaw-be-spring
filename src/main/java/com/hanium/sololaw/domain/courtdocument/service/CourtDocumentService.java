/* 
 * Copyright (c) HANIUM SOLOLAW 
 */
package com.hanium.sololaw.domain.courtdocument.service;

import org.springframework.web.multipart.MultipartFile;

import com.hanium.sololaw.domain.courtdocument.dto.response.CourtDocumentAnalysisResponse;
import com.hanium.sololaw.domain.user.entity.User;

public interface CourtDocumentService {

  /**
   * 법원 문서(기일통지서·보정명령·소장부본송달 등) 파일을 분석해 일정 정보를 추출한다. 저장은 하지 않는다.
   *
   * @param user 요청 사용자
   * @param file 업로드된 법원 문서(PDF/JPG/PNG)
   * @return 추출 결과
   */
  CourtDocumentAnalysisResponse analyze(User user, MultipartFile file);
}
