/* 
 * Copyright (c) HANIUM SOLOLAW 
 */
package com.hanium.sololaw.domain.precedent.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.hanium.sololaw.domain.cases.entity.Case;
import com.hanium.sololaw.domain.cases.exception.CaseErrorCode;
import com.hanium.sololaw.domain.cases.repository.CaseRepository;
import com.hanium.sololaw.domain.document.entity.Document;
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

@ExtendWith(MockitoExtension.class)
class PrecedentCitationServiceTest {

  @Mock private CaseRepository caseRepository;
  @Mock private DocumentRepository documentRepository;
  @Mock private PrecedentCitationRepository precedentCitationRepository;
  @Mock private PrecedentCitationMapper precedentCitationMapper;

  @InjectMocks private PrecedentCitationServiceImpl precedentCitationService;

  @Test
  void create_throwsNotFound_whenCaseNotOwned() {
    User user = User.builder().id(1L).build();
    CreatePrecedentCitationRequest request =
        new CreatePrecedentCitationRequest(
            "2024다12345", "임대차보증금 반환 청구", null, null, null, null, null, 999L, null);
    when(caseRepository.findByIdAndUserId(999L, 1L)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> precedentCitationService.create(user, request))
        .isInstanceOf(CustomException.class)
        .extracting(e -> ((CustomException) e).getErrorCode())
        .isEqualTo(CaseErrorCode.CASE_NOT_FOUND);
  }

  @Test
  void create_savesCitation_whenCaseOwned() {
    User user = User.builder().id(1L).build();
    CreatePrecedentCitationRequest request =
        new CreatePrecedentCitationRequest(
            "2024다12345", "임대차보증금 반환 청구", null, null, null, null, null, 5L, null);
    Case ownedCase = Case.builder().id(5L).userId(1L).build();
    PrecedentCitation newCitation = PrecedentCitation.builder().userId(1L).caseId(5L).build();
    PrecedentCitation savedCitation =
        PrecedentCitation.builder().id(20L).userId(1L).caseId(5L).build();

    when(caseRepository.findByIdAndUserId(5L, 1L)).thenReturn(Optional.of(ownedCase));
    when(precedentCitationMapper.toEntity(1L, request)).thenReturn(newCitation);
    when(precedentCitationRepository.save(newCitation)).thenReturn(savedCitation);
    when(precedentCitationMapper.toResponse(savedCitation))
        .thenReturn(PrecedentCitationResponse.builder().id(20L).build());

    PrecedentCitationResponse result = precedentCitationService.create(user, request);

    assertThat(result.id()).isEqualTo(20L);
  }

  @Test
  void getList_delegatesToRepositoryWithFilters() {
    User user = User.builder().id(1L).build();
    PrecedentCitation citation = PrecedentCitation.builder().id(20L).userId(1L).caseId(5L).build();
    when(precedentCitationRepository.findAllByUserIdAndFilters(1L, 5L, null))
        .thenReturn(List.of(citation));
    when(precedentCitationMapper.toResponseList(List.of(citation)))
        .thenReturn(List.of(PrecedentCitationResponse.builder().id(20L).build()));

    List<PrecedentCitationResponse> result = precedentCitationService.getList(user, 5L, null);

    assertThat(result).hasSize(1);
  }

  @Test
  void linkDocument_throwsNotFound_whenNotOwned() {
    User user = User.builder().id(1L).build();
    when(precedentCitationRepository.findByIdAndUserId(999L, 1L)).thenReturn(Optional.empty());

    assertThatThrownBy(
            () -> precedentCitationService.linkDocument(user, 999L, new LinkDocumentRequest(1L)))
        .isInstanceOf(CustomException.class)
        .extracting(e -> ((CustomException) e).getErrorCode())
        .isEqualTo(PrecedentErrorCode.PRECEDENT_CITATION_NOT_FOUND);
  }

  @Test
  void linkDocument_throwsNotFound_whenDocumentNotOwned() {
    User user = User.builder().id(1L).build();
    PrecedentCitation citation = PrecedentCitation.builder().id(20L).userId(1L).caseId(5L).build();
    when(precedentCitationRepository.findByIdAndUserId(20L, 1L)).thenReturn(Optional.of(citation));
    when(documentRepository.findByIdAndUserId(999L, 1L)).thenReturn(Optional.empty());

    assertThatThrownBy(
            () -> precedentCitationService.linkDocument(user, 20L, new LinkDocumentRequest(999L)))
        .isInstanceOf(CustomException.class)
        .extracting(e -> ((CustomException) e).getErrorCode())
        .isEqualTo(DocumentErrorCode.DOCUMENT_NOT_FOUND);
  }

  @Test
  void linkDocument_setsDocumentId() {
    User user = User.builder().id(1L).build();
    PrecedentCitation citation = PrecedentCitation.builder().id(20L).userId(1L).caseId(5L).build();
    Document document = Document.builder().id(7L).userId(1L).build();
    when(precedentCitationRepository.findByIdAndUserId(20L, 1L)).thenReturn(Optional.of(citation));
    when(documentRepository.findByIdAndUserId(7L, 1L)).thenReturn(Optional.of(document));
    when(precedentCitationMapper.toResponse(citation))
        .thenReturn(PrecedentCitationResponse.builder().id(20L).documentId(7L).build());

    precedentCitationService.linkDocument(user, 20L, new LinkDocumentRequest(7L));

    assertThat(citation.getDocumentId()).isEqualTo(7L);
  }

  @Test
  void delete_removesCitation() {
    User user = User.builder().id(1L).build();
    PrecedentCitation citation = PrecedentCitation.builder().id(20L).userId(1L).build();
    when(precedentCitationRepository.findByIdAndUserId(20L, 1L)).thenReturn(Optional.of(citation));

    precedentCitationService.delete(user, 20L);

    verify(precedentCitationRepository).delete(citation);
  }
}
