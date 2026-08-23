/* 
 * Copyright (c) HANIUM SOLOLAW 
 */
package com.hanium.sololaw.domain.precedent.mapper;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Component;

import com.hanium.sololaw.domain.precedent.dto.request.CreatePrecedentCitationRequest;
import com.hanium.sololaw.domain.precedent.dto.response.PrecedentCitationResponse;
import com.hanium.sololaw.domain.precedent.entity.PrecedentCitation;

@Component
public class PrecedentCitationMapper {

  /**
   * @param userId : 소유자 사용자 ID
   * @param request : 변환할 CreatePrecedentCitationRequest
   * @return : 변환된 PrecedentCitation Entity
   */
  public PrecedentCitation toEntity(Long userId, CreatePrecedentCitationRequest request) {
    return PrecedentCitation.builder()
        .userId(userId)
        .serialId(request.serialId())
        .name(request.name())
        .caseNo(request.caseNo())
        .court(request.court())
        .decisionDate(request.decisionDate())
        .category(request.category())
        .referenceNote(request.referenceNote())
        .detailUrl(request.detailUrl())
        .caseId(request.caseId())
        .documentId(request.documentId())
        .citedAt(LocalDateTime.now())
        .build();
  }

  /**
   * @param citation : 변환할 PrecedentCitation Entity
   */
  public PrecedentCitationResponse toResponse(PrecedentCitation citation) {
    return PrecedentCitationResponse.builder()
        .id(citation.getId())
        .serialId(citation.getSerialId())
        .name(citation.getName())
        .caseNo(citation.getCaseNo())
        .court(citation.getCourt())
        .decisionDate(citation.getDecisionDate())
        .category(citation.getCategory())
        .referenceNote(citation.getReferenceNote())
        .detailUrl(citation.getDetailUrl())
        .caseId(citation.getCaseId())
        .documentId(citation.getDocumentId())
        .citedAt(citation.getCitedAt())
        .createdAt(citation.getCreatedAt())
        .modifiedAt(citation.getModifiedAt())
        .build();
  }

  /**
   * @param citations : 변환할 PrecedentCitation Entity 목록
   */
  public List<PrecedentCitationResponse> toResponseList(List<PrecedentCitation> citations) {
    return citations.stream().map(this::toResponse).toList();
  }
}
