/* 
 * Copyright (c) HANIUM SOLOLAW 
 */
package com.hanium.sololaw.domain.document.service;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hanium.sololaw.domain.cases.exception.CaseErrorCode;
import com.hanium.sololaw.domain.cases.repository.CaseRepository;
import com.hanium.sololaw.domain.document.dto.request.CreateDocumentRequest;
import com.hanium.sololaw.domain.document.dto.request.SaveGenerationResultRequest;
import com.hanium.sololaw.domain.document.dto.request.UpdateDocumentRequest;
import com.hanium.sololaw.domain.document.dto.request.UpdateDocumentStatusRequest;
import com.hanium.sololaw.domain.document.dto.response.DocumentDetailResponse;
import com.hanium.sololaw.domain.document.dto.response.DocumentGenerationJobResponse;
import com.hanium.sololaw.domain.document.dto.response.DocumentResponse;
import com.hanium.sololaw.domain.document.entity.Document;
import com.hanium.sololaw.domain.document.entity.DocumentGenerationJob;
import com.hanium.sololaw.domain.document.entity.enums.DocType;
import com.hanium.sololaw.domain.document.entity.enums.DocumentStatus;
import com.hanium.sololaw.domain.document.entity.enums.JobStatus;
import com.hanium.sololaw.domain.document.exception.DocumentErrorCode;
import com.hanium.sololaw.domain.document.mapper.DocumentGenerationJobMapper;
import com.hanium.sololaw.domain.document.mapper.DocumentMapper;
import com.hanium.sololaw.domain.document.repository.DocumentGenerationJobRepository;
import com.hanium.sololaw.domain.document.repository.DocumentRepository;
import com.hanium.sololaw.domain.user.entity.User;
import com.hanium.sololaw.global.common.OffsetPageResponse;
import com.hanium.sololaw.global.exception.CustomException;
import com.hanium.sololaw.global.storage.S3Uploader;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentServiceImpl implements DocumentService {

  private static final Duration DOWNLOAD_URL_EXPIRY = Duration.ofMinutes(10);

  private final CaseRepository caseRepository;
  private final DocumentRepository documentRepository;
  private final DocumentGenerationJobRepository documentGenerationJobRepository;
  private final DocumentMapper documentMapper;
  private final DocumentGenerationJobMapper documentGenerationJobMapper;
  private final S3Uploader s3Uploader;

  @Override
  @Transactional
  public DocumentResponse createDraft(User user, Long caseId, CreateDocumentRequest request) {
    log.info(
        "[DocumentService] createDraft() - START | userId: {}, caseId: {}, docType: {}",
        user.getId(),
        caseId,
        request.docType());

    /*
       1. 사건 소유자 검증
    */
    caseRepository
        .findByIdAndUserId(caseId, user.getId())
        .orElseThrow(() -> new CustomException(CaseErrorCode.CASE_NOT_FOUND));

    /*
       2. 같은 case+docType 기존 최신본 내림
    */
    documentRepository
        .findByCaseIdAndDocTypeAndIsLatestTrue(caseId, request.docType())
        .ifPresent(previous -> previous.updateIsLatest(false));

    /*
       3. 신규 초안 저장
    */
    Document savedDocument =
        documentRepository.save(documentMapper.toEntity(caseId, user.getId(), request));

    /*
       4. ResponseDto Mapping
    */
    DocumentResponse result = documentMapper.toResponse(savedDocument);

    log.info("[DocumentService] createDraft() - END | documentId: {}", savedDocument.getId());
    return result;
  }

  @Override
  @Transactional(readOnly = true)
  public OffsetPageResponse<DocumentResponse> getList(
      User user,
      Long caseId,
      DocType docType,
      DocumentStatus status,
      Boolean isLatest,
      Pageable pageable) {
    log.info(
        "[DocumentService] getList() - START | userId: {}, caseId: {}, docType: {}, status: {}, isLatest: {}",
        user.getId(),
        caseId,
        docType,
        status,
        isLatest);

    /*
       1. 필터 조회
    */
    Page<Document> pageResult =
        documentRepository.findAllByUserIdAndFilters(
            user.getId(), caseId, docType, status, isLatest, pageable);

    /*
       2. ResponseDto Mapping 및 OffsetPageResponse 래핑
    */
    List<DocumentResponse> content = documentMapper.toResponseList(pageResult.getContent());
    OffsetPageResponse<DocumentResponse> result =
        OffsetPageResponse.of(
            content,
            pageResult.getTotalElements(),
            pageable.getPageNumber(),
            pageable.getPageSize());

    log.info(
        "[DocumentService] getList() - END | totalElements: {}", pageResult.getTotalElements());
    return result;
  }

  @Override
  @Transactional(readOnly = true)
  public DocumentDetailResponse getDetail(User user, Long documentId) {
    log.info(
        "[DocumentService] getDetail() - START | userId: {}, documentId: {}",
        user.getId(),
        documentId);

    /*
       1. 문서 조회 및 소유자 검증
    */
    Document document = findOwnedDocument(documentId, user.getId());

    /*
       2. ResponseDto Mapping
    */
    DocumentDetailResponse result = documentMapper.toDetailResponse(document);

    log.info("[DocumentService] getDetail() - END | documentId: {}", documentId);
    return result;
  }

  @Override
  @Transactional
  public DocumentResponse updateDraft(User user, Long documentId, UpdateDocumentRequest request) {
    log.info(
        "[DocumentService] updateDraft() - START | userId: {}, documentId: {}",
        user.getId(),
        documentId);

    /*
       1. 문서 조회 및 소유자 검증
    */
    Document document = findOwnedDocument(documentId, user.getId());

    /*
       2. 생성 중(RUNNING) 로그 존재 시 409
    */
    documentGenerationJobRepository
        .findByDocumentIdAndStatus(documentId, JobStatus.RUNNING)
        .ifPresent(
            job -> {
              throw new CustomException(DocumentErrorCode.DOCUMENT_LOCKED);
            });

    /*
       3. 초안 수정
       - null인 필드는 기존 값을 유지한다.
    */
    document.updateDraft(
        request.title() != null ? request.title() : document.getTitle(),
        request.content() != null
            ? documentMapper.toJson(request.content())
            : document.getContent());

    /*
       4. ResponseDto Mapping
    */
    DocumentResponse result = documentMapper.toResponse(document);

    log.info("[DocumentService] updateDraft() - END | documentId: {}", documentId);
    return result;
  }

  @Override
  @Transactional
  public DocumentResponse saveResult(
      User user, Long documentId, SaveGenerationResultRequest request) {
    log.info(
        "[DocumentService] saveResult() - START | userId: {}, documentId: {}",
        user.getId(),
        documentId);

    /*
       1. 문서 조회 및 소유자 검증
    */
    Document document = findOwnedDocument(documentId, user.getId());

    /*
       2. 생성 결과를 텍스트 파일로 S3 업로드
    */
    String key = "documents/%d/%s.txt".formatted(documentId, UUID.randomUUID());
    byte[] fileBytes = request.generatedText().getBytes(StandardCharsets.UTF_8);
    String savedKey = s3Uploader.upload(key, fileBytes, "text/plain; charset=UTF-8");

    /*
       3. 생성 결과 저장
       - generated_content/generated_text/file_url을 채우고 generated_at을 기록해 확정한다.
    */
    document.saveResult(
        documentMapper.toJson(request.generatedContent()), request.generatedText(), savedKey);

    /*
       4. 생성 로그(선택) SUCCEEDED로 갱신
    */
    documentGenerationJobRepository
        .findFirstByDocumentIdOrderByCreatedAtDesc(documentId)
        .ifPresent(DocumentGenerationJob::markSucceeded);

    /*
       5. ResponseDto Mapping
    */
    DocumentResponse result = documentMapper.toResponse(document);

    log.info("[DocumentService] saveResult() - END | documentId: {}", documentId);
    return result;
  }

  @Override
  @Transactional
  public DocumentResponse updateStatus(
      User user, Long documentId, UpdateDocumentStatusRequest request) {
    log.info(
        "[DocumentService] updateStatus() - START | userId: {}, documentId: {}, status: {}",
        user.getId(),
        documentId,
        request.status());

    /*
       1. 문서 조회 및 소유자 검증
    */
    Document document = findOwnedDocument(documentId, user.getId());

    /*
       2. 상태 변경
    */
    document.updateStatus(request.status());

    /*
       3. ResponseDto Mapping
    */
    DocumentResponse result = documentMapper.toResponse(document);

    log.info("[DocumentService] updateStatus() - END | documentId: {}", documentId);
    return result;
  }

  @Override
  @Transactional(readOnly = true)
  public String getDownloadUrl(User user, Long documentId) {
    log.info(
        "[DocumentService] getDownloadUrl() - START | userId: {}, documentId: {}",
        user.getId(),
        documentId);

    /*
       1. 문서 조회 및 소유자 검증
    */
    Document document = findOwnedDocument(documentId, user.getId());

    /*
       2. 파일 존재 여부 확인 및 presigned URL 발급
       - 아직 AI 생성이 완료되지 않아 file_url이 없으면 404로 거절한다.
    */
    if (document.getFileUrl() == null) {
      throw new CustomException(DocumentErrorCode.DOCUMENT_FILE_NOT_AVAILABLE);
    }
    String presignedUrl =
        s3Uploader.generatePresignedGetUrl(document.getFileUrl(), DOWNLOAD_URL_EXPIRY);

    log.info("[DocumentService] getDownloadUrl() - END | documentId: {}", documentId);
    return presignedUrl;
  }

  @Override
  @Transactional
  public void delete(User user, Long documentId) {
    log.info(
        "[DocumentService] delete() - START | userId: {}, documentId: {}",
        user.getId(),
        documentId);

    /*
       1. 문서 조회 및 소유자 검증
    */
    Document document = findOwnedDocument(documentId, user.getId());

    /*
       2. S3 파일 삭제
       - AI 생성 결과가 있어 파일이 실제로 업로드된 경우에만 정리한다.
    */
    if (document.getFileUrl() != null) {
      s3Uploader.deleteObject(document.getFileUrl());
    }

    /*
       3. 문서 삭제
       - document_generation_jobs는 DB CASCADE 제약으로 함께 삭제된다.
    */
    documentRepository.delete(document);

    log.info("[DocumentService] delete() - END | documentId: {}", documentId);
  }

  @Override
  @Transactional(readOnly = true)
  public DocumentGenerationJobResponse getGenerationJob(User user, Long jobId) {
    log.info(
        "[DocumentService] getGenerationJob() - START | userId: {}, jobId: {}",
        user.getId(),
        jobId);

    /*
       1. 생성 로그 조회 및 소유자 검증
    */
    DocumentGenerationJob job =
        documentGenerationJobRepository
            .findByIdAndUserId(jobId, user.getId())
            .orElseThrow(() -> new CustomException(DocumentErrorCode.GENERATION_JOB_NOT_FOUND));

    /*
       2. ResponseDto Mapping
    */
    DocumentGenerationJobResponse result = documentGenerationJobMapper.toResponse(job);

    log.info("[DocumentService] getGenerationJob() - END | jobId: {}", jobId);
    return result;
  }

  private Document findOwnedDocument(Long documentId, Long userId) {
    return documentRepository
        .findByIdAndUserId(documentId, userId)
        .orElseThrow(() -> new CustomException(DocumentErrorCode.DOCUMENT_NOT_FOUND));
  }
}
