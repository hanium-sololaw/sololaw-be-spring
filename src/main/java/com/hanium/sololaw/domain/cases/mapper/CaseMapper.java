/* 
 * Copyright (c) HANIUM SOLOLAW 
 */
package com.hanium.sololaw.domain.cases.mapper;

import java.util.List;

import org.springframework.stereotype.Component;

import com.hanium.sololaw.domain.cases.dto.request.CreateCaseRequest;
import com.hanium.sololaw.domain.cases.dto.response.CaseDetailResponse;
import com.hanium.sololaw.domain.cases.dto.response.CasePartySummaryResponse;
import com.hanium.sololaw.domain.cases.dto.response.CaseResponse;
import com.hanium.sololaw.domain.cases.entity.Case;

@Component
public class CaseMapper {

  /**
   * @param userId : 소유자 사용자 ID
   * @param request : 변환할 CreateCaseRequest
   * @return : 변환된 Case Entity
   */
  public Case toEntity(Long userId, CreateCaseRequest request) {
    return Case.builder()
        .userId(userId)
        .title(request.title())
        .caseType(request.caseType())
        .claimAmount(request.claimAmount())
        .court(request.court())
        .caseNumber(request.caseNumber())
        .build();
  }

  /**
   * @param caseEntity : 변환할 Case Entity
   */
  public CaseResponse toResponse(Case caseEntity) {
    return CaseResponse.builder()
        .id(caseEntity.getId())
        .caseNumber(caseEntity.getCaseNumber())
        .title(caseEntity.getTitle())
        .caseType(caseEntity.getCaseType())
        .status(caseEntity.getStatus())
        .progressRate(caseEntity.getProgressRate())
        .court(caseEntity.getCourt())
        .claimAmount(caseEntity.getClaimAmount())
        .openedAt(caseEntity.getOpenedAt())
        .filingMethod(caseEntity.getFilingMethod())
        .createdAt(caseEntity.getCreatedAt())
        .modifiedAt(caseEntity.getModifiedAt())
        .build();
  }

  /**
   * @param cases : 변환할 Case Entity 목록
   */
  public List<CaseResponse> toResponseList(List<Case> cases) {
    return cases.stream().map(this::toResponse).toList();
  }

  /**
   * 최근활동 집계는 08번 activity_logs 도메인 미구현으로 0 고정값을 받는다.
   *
   * @param caseEntity : 변환할 Case Entity
   * @param parties : 당사자 요약 목록
   * @param documentCount : 문서 개수
   * @param evidenceCount : 증빙자료 개수
   * @param scheduleCount : 일정 개수
   * @param recentActivityCount : 최근 활동 개수, 0 고정
   */
  public CaseDetailResponse toDetailResponse(
      Case caseEntity,
      List<CasePartySummaryResponse> parties,
      int documentCount,
      int evidenceCount,
      int scheduleCount,
      int recentActivityCount) {
    return CaseDetailResponse.builder()
        .id(caseEntity.getId())
        .caseNumber(caseEntity.getCaseNumber())
        .title(caseEntity.getTitle())
        .caseType(caseEntity.getCaseType())
        .status(caseEntity.getStatus())
        .progressRate(caseEntity.getProgressRate())
        .court(caseEntity.getCourt())
        .claimAmount(caseEntity.getClaimAmount())
        .openedAt(caseEntity.getOpenedAt())
        .filingMethod(caseEntity.getFilingMethod())
        .createdAt(caseEntity.getCreatedAt())
        .modifiedAt(caseEntity.getModifiedAt())
        .parties(parties)
        .documentCount(documentCount)
        .evidenceCount(evidenceCount)
        .scheduleCount(scheduleCount)
        .recentActivityCount(recentActivityCount)
        .build();
  }
}
