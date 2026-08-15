/* 
 * Copyright (c) HANIUM SOLOLAW 
 */
package com.hanium.sololaw.domain.cases.service;

import java.util.List;

import com.hanium.sololaw.domain.cases.dto.request.CreatePartyRequest;
import com.hanium.sololaw.domain.cases.dto.request.UpdatePartyRequest;
import com.hanium.sololaw.domain.cases.dto.response.CasePartyResponse;
import com.hanium.sololaw.domain.user.entity.User;

public interface CasePartyService {

  /**
   * 사건의 당사자 목록을 조회합니다(공동소송 시 원고 다수 포함).
   *
   * @param user : 로그인 사용자
   * @param caseId : 소속 사건 ID
   * @return : CasePartyResponse 목록
   */
  List<CasePartyResponse> getParties(User user, Long caseId);

  /**
   * 사건에 당사자를 추가합니다("+ 원고 추가(공동소송)" 등).
   *
   * @param user : 로그인 사용자
   * @param caseId : 소속 사건 ID
   * @param request : 당사자 추가 요청
   * @return : 생성된 CasePartyResponse
   */
  CasePartyResponse addParty(User user, Long caseId, CreatePartyRequest request);

  /**
   * 당사자 상세정보(주민등록번호·주소·연락처)를 수정합니다. residentNo는 AES 암호화 후 저장합니다.
   *
   * @param user : 로그인 사용자
   * @param caseId : 소속 사건 ID
   * @param partyId : 수정할 당사자 ID
   * @param request : 당사자 상세정보 수정 요청
   * @return : 수정된 CasePartyResponse
   */
  CasePartyResponse updatePartyDetails(
      User user, Long caseId, Long partyId, UpdatePartyRequest request);

  /**
   * 당사자를 삭제합니다.
   *
   * @param user : 로그인 사용자
   * @param caseId : 소속 사건 ID
   * @param partyId : 삭제할 당사자 ID
   */
  void deleteParty(User user, Long caseId, Long partyId);
}
