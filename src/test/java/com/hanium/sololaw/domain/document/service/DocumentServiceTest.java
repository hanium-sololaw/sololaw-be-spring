/* 
 * Copyright (c) HANIUM SOLOLAW 
 */
package com.hanium.sololaw.domain.document.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.hanium.sololaw.domain.cases.entity.Case;
import com.hanium.sololaw.domain.cases.exception.CaseErrorCode;
import com.hanium.sololaw.domain.cases.repository.CaseRepository;
import com.hanium.sololaw.domain.document.dto.request.CreateDocumentRequest;
import com.hanium.sololaw.domain.document.dto.request.SaveGenerationResultRequest;
import com.hanium.sololaw.domain.document.dto.request.UpdateDocumentRequest;
import com.hanium.sololaw.domain.document.dto.response.DocumentResponse;
import com.hanium.sololaw.domain.document.entity.Document;
import com.hanium.sololaw.domain.document.entity.DocumentGenerationJob;
import com.hanium.sololaw.domain.document.entity.enums.DocType;
import com.hanium.sololaw.domain.document.entity.enums.JobStatus;
import com.hanium.sololaw.domain.document.exception.DocumentErrorCode;
import com.hanium.sololaw.domain.document.mapper.DocumentGenerationJobMapper;
import com.hanium.sololaw.domain.document.mapper.DocumentMapper;
import com.hanium.sololaw.domain.document.repository.DocumentGenerationJobRepository;
import com.hanium.sololaw.domain.document.repository.DocumentRepository;
import com.hanium.sololaw.domain.user.entity.User;
import com.hanium.sololaw.global.exception.CustomException;
import com.hanium.sololaw.global.storage.S3Uploader;

import tools.jackson.databind.node.StringNode;

@ExtendWith(MockitoExtension.class)
class DocumentServiceTest {

  @Mock private CaseRepository caseRepository;
  @Mock private DocumentRepository documentRepository;
  @Mock private DocumentGenerationJobRepository documentGenerationJobRepository;
  @Mock private DocumentMapper documentMapper;
  @Mock private DocumentGenerationJobMapper documentGenerationJobMapper;
  @Mock private S3Uploader s3Uploader;

  @InjectMocks private DocumentServiceImpl documentService;

  @Test
  void createDraft_demotesPreviousLatest_whenSameCaseAndDocType() {
    User user = User.builder().id(1L).build();
    CreateDocumentRequest request =
        new CreateDocumentRequest(DocType.COMPLAINT, null, "임대차보증금 반환 소장", null);
    Case ownedCase = Case.builder().id(5L).userId(1L).build();
    Document previousLatest =
        Document.builder().id(10L).caseId(5L).docType(DocType.COMPLAINT).isLatest(true).build();
    Document newDraft = Document.builder().caseId(5L).userId(1L).docType(DocType.COMPLAINT).build();
    Document savedDraft = Document.builder().id(11L).caseId(5L).userId(1L).build();

    when(caseRepository.findByIdAndUserId(5L, 1L)).thenReturn(Optional.of(ownedCase));
    when(documentRepository.findByCaseIdAndDocTypeAndIsLatestTrue(5L, DocType.COMPLAINT))
        .thenReturn(Optional.of(previousLatest));
    when(documentMapper.toEntity(5L, 1L, request)).thenReturn(newDraft);
    when(documentRepository.save(newDraft)).thenReturn(savedDraft);
    when(documentMapper.toResponse(savedDraft))
        .thenReturn(DocumentResponse.builder().id(11L).build());

    documentService.createDraft(user, 5L, request);

    assertThat(previousLatest.getIsLatest()).isFalse();
  }

  @Test
  void createDraft_throwsNotFound_whenCaseNotOwned() {
    User user = User.builder().id(1L).build();
    CreateDocumentRequest request = new CreateDocumentRequest(DocType.ANSWER, null, "답변서", null);
    when(caseRepository.findByIdAndUserId(999L, 1L)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> documentService.createDraft(user, 999L, request))
        .isInstanceOf(CustomException.class)
        .extracting(e -> ((CustomException) e).getErrorCode())
        .isEqualTo(CaseErrorCode.CASE_NOT_FOUND);
  }

  @Test
  void updateDraft_throwsLocked_whenRunningJobExists() {
    User user = User.builder().id(1L).build();
    Document document = Document.builder().id(20L).userId(1L).build();
    UpdateDocumentRequest request = new UpdateDocumentRequest("제목", null);
    when(documentRepository.findByIdAndUserId(20L, 1L)).thenReturn(Optional.of(document));
    when(documentGenerationJobRepository.findByDocumentIdAndStatus(20L, JobStatus.RUNNING))
        .thenReturn(Optional.of(DocumentGenerationJob.builder().id(1L).build()));

    assertThatThrownBy(() -> documentService.updateDraft(user, 20L, request))
        .isInstanceOf(CustomException.class)
        .extracting(e -> ((CustomException) e).getErrorCode())
        .isEqualTo(DocumentErrorCode.DOCUMENT_LOCKED);
  }

  @Test
  void saveResult_uploadsToS3AndMarksJobSucceeded() {
    User user = User.builder().id(1L).build();
    Document document = Document.builder().id(30L).userId(1L).build();
    SaveGenerationResultRequest request =
        new SaveGenerationResultRequest("생성된 소장 본문", new StringNode("sections"));
    DocumentGenerationJob job = DocumentGenerationJob.builder().id(2L).documentId(30L).build();

    when(documentRepository.findByIdAndUserId(30L, 1L)).thenReturn(Optional.of(document));
    when(s3Uploader.upload(anyString(), any(byte[].class), anyString()))
        .thenReturn("documents/30/key.txt");
    when(documentGenerationJobRepository.findFirstByDocumentIdOrderByCreatedAtDesc(30L))
        .thenReturn(Optional.of(job));
    when(documentMapper.toResponse(document))
        .thenReturn(DocumentResponse.builder().id(30L).build());

    documentService.saveResult(user, 30L, request);

    assertThat(document.getFileUrl()).isEqualTo("documents/30/key.txt");
    assertThat(document.getGeneratedText()).isEqualTo("생성된 소장 본문");
    assertThat(document.getGeneratedAt()).isNotNull();
    assertThat(job.getStatus()).isEqualTo(JobStatus.SUCCEEDED);
  }

  @Test
  void getDownloadUrl_throwsFileNotAvailable_whenNotGeneratedYet() {
    User user = User.builder().id(1L).build();
    Document document = Document.builder().id(40L).userId(1L).build();
    when(documentRepository.findByIdAndUserId(40L, 1L)).thenReturn(Optional.of(document));

    assertThatThrownBy(() -> documentService.getDownloadUrl(user, 40L))
        .isInstanceOf(CustomException.class)
        .extracting(e -> ((CustomException) e).getErrorCode())
        .isEqualTo(DocumentErrorCode.DOCUMENT_FILE_NOT_AVAILABLE);
  }

  @Test
  void getDownloadUrl_returnsPresignedUrl_whenFileExists() {
    User user = User.builder().id(1L).build();
    Document document =
        Document.builder().id(41L).userId(1L).fileUrl("documents/41/key.txt").build();
    when(documentRepository.findByIdAndUserId(41L, 1L)).thenReturn(Optional.of(document));
    when(s3Uploader.generatePresignedGetUrl("documents/41/key.txt", Duration.ofMinutes(10)))
        .thenReturn("https://s3.example.com/presigned");

    String result = documentService.getDownloadUrl(user, 41L);

    assertThat(result).isEqualTo("https://s3.example.com/presigned");
  }

  @Test
  void deleteDocument_throwsNotFound_whenNotOwned() {
    User user = User.builder().id(1L).build();
    when(documentRepository.findByIdAndUserId(999L, 1L)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> documentService.delete(user, 999L))
        .isInstanceOf(CustomException.class)
        .extracting(e -> ((CustomException) e).getErrorCode())
        .isEqualTo(DocumentErrorCode.DOCUMENT_NOT_FOUND);
  }
}
