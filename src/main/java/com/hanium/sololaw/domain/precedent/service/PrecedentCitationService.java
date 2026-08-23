/* 
 * Copyright (c) HANIUM SOLOLAW 
 */
package com.hanium.sololaw.domain.precedent.service;

import java.util.List;

import com.hanium.sololaw.domain.precedent.dto.request.CreatePrecedentCitationRequest;
import com.hanium.sololaw.domain.precedent.dto.request.LinkDocumentRequest;
import com.hanium.sololaw.domain.precedent.dto.response.PrecedentCitationResponse;
import com.hanium.sololaw.domain.user.entity.User;

public interface PrecedentCitationService {

  /**
   * 판례 인용을 추가합니다. caseId 소유자를 검증합니다.
   *
   * @param user : 로그인 사용자
   * @param request : 인용 추가 요청
   * @return : 추가된 PrecedentCitationResponse
   */
  PrecedentCitationResponse create(User user, CreatePrecedentCitationRequest request);

  /**
   * 로그인 사용자의 인용 목록을 조회합니다.
   *
   * @param user : 로그인 사용자
   * @param caseId : 사건 필터(선택)
   * @param documentId : 문서 필터(선택)
   * @return : PrecedentCitationResponse 목록(인용 시각 내림차순)
   */
  List<PrecedentCitationResponse> getList(User user, Long caseId, Long documentId);

  /**
   * 인용을 특정 문서에 연결합니다.
   *
   * @param user : 로그인 사용자
   * @param citationId : 연결할 인용 ID
   * @param request : 문서 연결 요청
   * @return : 수정된 PrecedentCitationResponse
   */
  PrecedentCitationResponse linkDocument(User user, Long citationId, LinkDocumentRequest request);

  /**
   * 인용을 삭제합니다.
   *
   * @param user : 로그인 사용자
   * @param citationId : 삭제할 인용 ID
   */
  void delete(User user, Long citationId);
}
