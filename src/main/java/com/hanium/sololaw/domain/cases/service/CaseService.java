/* 
 * Copyright (c) HANIUM SOLOLAW 
 */
package com.hanium.sololaw.domain.cases.service;

import org.springframework.data.domain.Pageable;

import com.hanium.sololaw.domain.cases.dto.request.CreateCaseRequest;
import com.hanium.sololaw.domain.cases.dto.request.UpdateCaseRequest;
import com.hanium.sololaw.domain.cases.dto.request.UpdateCaseStatusRequest;
import com.hanium.sololaw.domain.cases.dto.response.CaseDetailResponse;
import com.hanium.sololaw.domain.cases.dto.response.CaseResponse;
import com.hanium.sololaw.domain.cases.entity.enums.CaseStatus;
import com.hanium.sololaw.domain.cases.entity.enums.CaseType;
import com.hanium.sololaw.domain.user.entity.User;
import com.hanium.sololaw.global.common.OffsetPageResponse;

public interface CaseService {

  /**
   * 사건을 생성합니다. opponentName으로 DEFENDANT 당사자 행을, 로그인 사용자 이름으로 is_self=true인 PLAINTIFF 당사자 행을 함께 자동
   * 생성합니다. caseType이 미정이 아니면 표준 절차 6단계를 startingStage 기준으로 자동 시드합니다.
   *
   * @param user : 로그인 사용자
   * @param request : 사건 생성 요청
   * @return : 생성된 CaseResponse
   */
  CaseResponse createCase(User user, CreateCaseRequest request);

  /**
   * 로그인 사용자의 사건 목록을 조회합니다.
   *
   * @param user : 로그인 사용자
   * @param status : 상태 필터(선택)
   * @param caseType : 사건 유형 필터(선택)
   * @param pageable : page(0-base)/size/sort 쿼리 파라미터 바인딩
   * @return : OffsetPageResponse<CaseResponse>
   */
  OffsetPageResponse<CaseResponse> getCaseList(
      User user, CaseStatus status, CaseType caseType, Pageable pageable);

  /**
   * 사건 상세를 조회합니다. 당사자 요약과 문서/증빙/일정/최근활동 집계, 최근활동 5건(최신순)을 함께 반환합니다.
   *
   * @param user : 로그인 사용자
   * @param caseId : 조회할 사건 ID
   * @return : 조회된 CaseDetailResponse
   */
  CaseDetailResponse getCaseDetail(User user, Long caseId);

  /**
   * 사건 정보를 수정합니다. caseType이 미정(NULL)에서 특정 값으로 처음 채워지는 시점에 표준 절차 6단계를 1단계부터 자동 시드합니다.
   *
   * @param user : 로그인 사용자
   * @param caseId : 수정할 사건 ID
   * @param request : 사건 수정 요청
   * @return : 수정된 CaseResponse
   */
  CaseResponse updateCase(User user, Long caseId, UpdateCaseRequest request);

  /**
   * 사건 상태를 변경합니다. 변경 전·후 상태와 reason을 활동 기록(ActivityLog)으로 남깁니다.
   *
   * @param user : 로그인 사용자
   * @param caseId : 상태를 변경할 사건 ID
   * @param request : 상태 변경 요청
   * @return : 수정된 CaseResponse
   */
  CaseResponse updateCaseStatus(User user, Long caseId, UpdateCaseStatusRequest request);

  /**
   * 사건을 삭제합니다. case_parties/litigation_stages/case_todos는 DB CASCADE로 함께 삭제됩니다.
   *
   * @param user : 로그인 사용자
   * @param caseId : 삭제할 사건 ID
   */
  void deleteCase(User user, Long caseId);
}
