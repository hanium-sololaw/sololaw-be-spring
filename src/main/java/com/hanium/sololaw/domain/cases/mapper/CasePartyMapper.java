/* 
 * Copyright (c) HANIUM SOLOLAW 
 */
package com.hanium.sololaw.domain.cases.mapper;

import java.util.List;

import org.springframework.stereotype.Component;

import com.hanium.sololaw.domain.cases.dto.request.CreatePartyRequest;
import com.hanium.sololaw.domain.cases.dto.response.CasePartyResponse;
import com.hanium.sololaw.domain.cases.dto.response.CasePartySummaryResponse;
import com.hanium.sololaw.domain.cases.entity.CaseParty;
import com.hanium.sololaw.domain.cases.entity.enums.PartyRole;
import com.hanium.sololaw.global.crypto.AesEncryptor;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class CasePartyMapper {

  private final AesEncryptor aesEncryptor;

  /**
   * @param caseId : 소속 사건 ID
   * @param request : 변환할 CreatePartyRequest
   * @return : 변환된 CaseParty Entity
   */
  public CaseParty toEntity(Long caseId, CreatePartyRequest request) {
    return CaseParty.builder()
        .caseId(caseId)
        .partyRole(request.partyRole())
        .name(request.name())
        .isSelf(false)
        .build();
  }

  /**
   * 사건 생성 시 자동 생성되는 원고(나) 행을 만든다.
   *
   * @param caseId : 소속 사건 ID
   * @param userName : 로그인 사용자 이름
   * @return : 변환된 CaseParty Entity
   */
  public CaseParty toSelfEntity(Long caseId, String userName) {
    return CaseParty.builder()
        .caseId(caseId)
        .partyRole(PartyRole.PLAINTIFF)
        .name(userName)
        .isSelf(true)
        .build();
  }

  /**
   * 사건 생성 시 자동 생성되는 피고 행을 만든다.
   *
   * @param caseId : 소속 사건 ID
   * @param opponentName : 상대방 이름
   * @return : 변환된 CaseParty Entity
   */
  public CaseParty toDefendantEntity(Long caseId, String opponentName) {
    return CaseParty.builder()
        .caseId(caseId)
        .partyRole(PartyRole.DEFENDANT)
        .name(opponentName)
        .isSelf(false)
        .build();
  }

  /**
   * @param caseParty : 변환할 CaseParty Entity(residentNo는 마스킹 처리)
   */
  public CasePartyResponse toResponse(CaseParty caseParty) {
    return CasePartyResponse.builder()
        .id(caseParty.getId())
        .partyRole(caseParty.getPartyRole())
        .name(caseParty.getName())
        .residentNoMasked(aesEncryptor.mask(caseParty.getResidentNo()))
        .address(caseParty.getAddress())
        .phone(caseParty.getPhone())
        .isSelf(caseParty.getIsSelf())
        .createdAt(caseParty.getCreatedAt())
        .modifiedAt(caseParty.getModifiedAt())
        .build();
  }

  /**
   * @param caseParties : 변환할 CaseParty Entity 목록
   */
  public List<CasePartyResponse> toResponseList(List<CaseParty> caseParties) {
    return caseParties.stream().map(this::toResponse).toList();
  }

  /**
   * @param caseParty : 변환할 CaseParty Entity
   */
  public CasePartySummaryResponse toSummaryResponse(CaseParty caseParty) {
    return CasePartySummaryResponse.builder()
        .id(caseParty.getId())
        .partyRole(caseParty.getPartyRole())
        .name(caseParty.getName())
        .isSelf(caseParty.getIsSelf())
        .build();
  }

  /**
   * @param caseParties : 변환할 CaseParty Entity 목록
   */
  public List<CasePartySummaryResponse> toSummaryResponseList(List<CaseParty> caseParties) {
    return caseParties.stream().map(this::toSummaryResponse).toList();
  }
}
