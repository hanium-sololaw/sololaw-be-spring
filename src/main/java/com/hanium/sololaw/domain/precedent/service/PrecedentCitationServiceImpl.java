/* 
 * Copyright (c) HANIUM SOLOLAW 
 */
package com.hanium.sololaw.domain.precedent.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hanium.sololaw.domain.cases.exception.CaseErrorCode;
import com.hanium.sololaw.domain.cases.repository.CaseRepository;
import com.hanium.sololaw.domain.document.exception.DocumentErrorCode;
import com.hanium.sololaw.domain.document.repository.DocumentRepository;
import com.hanium.sololaw.domain.precedent.dto.request.CreatePrecedentCitationRequest;
import com.hanium.sololaw.domain.precedent.dto.request.LinkDocumentRequest;
import com.hanium.sololaw.domain.precedent.dto.response.PrecedentCitationResponse;
import com.hanium.sololaw.domain.precedent.entity.PrecedentCitation;
import com.hanium.sololaw.domain.precedent.exception.PrecedentErrorCode;
import com.hanium.sololaw.domain.precedent.mapper.PrecedentCitationMapper;
import com.hanium.sololaw.domain.precedent.repository.PrecedentCitationRepository;
import com.hanium.sololaw.domain.user.entity.User;
import com.hanium.sololaw.global.exception.CustomException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class PrecedentCitationServiceImpl implements PrecedentCitationService {

  private final CaseRepository caseRepository;
  private final DocumentRepository documentRepository;
  private final PrecedentCitationRepository precedentCitationRepository;
  private final PrecedentCitationMapper precedentCitationMapper;

  @Override
  @Transactional
  public PrecedentCitationResponse create(User user, CreatePrecedentCitationRequest request) {
    log.info(
        "[PrecedentCitationService] create() - START | userId: {}, caseId: {}",
        user.getId(),
        request.caseId());

    /*
       1. 사건 소유자 검증
    */
    caseRepository
        .findByIdAndUserId(request.caseId(), user.getId())
        .orElseThrow(() -> new CustomException(CaseErrorCode.CASE_NOT_FOUND));

    /*
       2. documentId가 있으면 문서 소유자 검증
    */
    if (request.documentId() != null) {
      documentRepository
          .findByIdAndUserId(request.documentId(), user.getId())
          .orElseThrow(() -> new CustomException(DocumentErrorCode.DOCUMENT_NOT_FOUND));
    }

    /*
       3. 인용 생성 및 저장
    */
    PrecedentCitation savedCitation =
        precedentCitationRepository.save(precedentCitationMapper.toEntity(user.getId(), request));

    /*
       4. ResponseDto Mapping
    */
    PrecedentCitationResponse result = precedentCitationMapper.toResponse(savedCitation);

    log.info("[PrecedentCitationService] create() - END | citationId: {}", savedCitation.getId());
    return result;
  }

  @Override
  @Transactional(readOnly = true)
  public List<PrecedentCitationResponse> getList(User user, Long caseId, Long documentId) {
    log.info(
        "[PrecedentCitationService] getList() - START | userId: {}, caseId: {}, documentId: {}",
        user.getId(),
        caseId,
        documentId);

    List<PrecedentCitation> citations =
        precedentCitationRepository.findAllByUserIdAndFilters(user.getId(), caseId, documentId);
    List<PrecedentCitationResponse> result = precedentCitationMapper.toResponseList(citations);

    log.info("[PrecedentCitationService] getList() - END | count: {}", result.size());
    return result;
  }

  @Override
  @Transactional
  public PrecedentCitationResponse linkDocument(
      User user, Long citationId, LinkDocumentRequest request) {
    log.info(
        "[PrecedentCitationService] linkDocument() - START | userId: {}, citationId: {}, documentId: {}",
        user.getId(),
        citationId,
        request.documentId());

    PrecedentCitation citation = findOwnedCitation(citationId, user.getId());
    documentRepository
        .findByIdAndUserId(request.documentId(), user.getId())
        .orElseThrow(() -> new CustomException(DocumentErrorCode.DOCUMENT_NOT_FOUND));
    citation.linkDocument(request.documentId());
    PrecedentCitationResponse result = precedentCitationMapper.toResponse(citation);

    log.info("[PrecedentCitationService] linkDocument() - END | citationId: {}", citationId);
    return result;
  }

  @Override
  @Transactional
  public void delete(User user, Long citationId) {
    log.info(
        "[PrecedentCitationService] delete() - START | userId: {}, citationId: {}",
        user.getId(),
        citationId);

    PrecedentCitation citation = findOwnedCitation(citationId, user.getId());
    precedentCitationRepository.delete(citation);

    log.info("[PrecedentCitationService] delete() - END | citationId: {}", citationId);
  }

  private PrecedentCitation findOwnedCitation(Long citationId, Long userId) {
    return precedentCitationRepository
        .findByIdAndUserId(citationId, userId)
        .orElseThrow(() -> new CustomException(PrecedentErrorCode.PRECEDENT_CITATION_NOT_FOUND));
  }
}
