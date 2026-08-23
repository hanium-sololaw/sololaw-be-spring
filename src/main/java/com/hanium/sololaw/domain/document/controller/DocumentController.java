/* 
 * Copyright (c) HANIUM SOLOLAW 
 */
package com.hanium.sololaw.domain.document.controller;

import java.net.URI;

import jakarta.validation.Valid;

import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.hanium.sololaw.domain.document.dto.request.CreateDocumentRequest;
import com.hanium.sololaw.domain.document.dto.request.SaveGenerationResultRequest;
import com.hanium.sololaw.domain.document.dto.request.UpdateDocumentRequest;
import com.hanium.sololaw.domain.document.dto.request.UpdateDocumentStatusRequest;
import com.hanium.sololaw.domain.document.dto.response.DocumentDetailResponse;
import com.hanium.sololaw.domain.document.dto.response.DocumentGenerationJobResponse;
import com.hanium.sololaw.domain.document.dto.response.DocumentResponse;
import com.hanium.sololaw.domain.document.entity.enums.DocType;
import com.hanium.sololaw.domain.document.entity.enums.DocumentStatus;
import com.hanium.sololaw.domain.document.service.DocumentService;
import com.hanium.sololaw.domain.user.entity.User;
import com.hanium.sololaw.global.common.BaseResponse;
import com.hanium.sololaw.global.common.OffsetPageResponse;
import com.hanium.sololaw.global.security.annotation.CurrentUser;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequiredArgsConstructor
@Tag(name = "Document", description = "AI 문서 생성 관련 기능을 제공하는 API")
public class DocumentController {

  private final DocumentService documentService;

  @Operation(
      summary = "[ 사용자 | 토큰 O | 문서 초안 생성 ]",
      description =
          """
            **Parameters**  \n
            docType(ANSWER 포함), applicationSubtype(선택), title(선택), content(JSONB 폼 입력), writingRate(선택, 0~100) \n
            \n
            같은 사건·같은 docType의 기존 최신본은 isLatest=false로 내려갑니다.
            """)
  @PostMapping("/api/cases/{caseId}/documents")
  public ResponseEntity<BaseResponse<DocumentResponse>> createDraft(
      @CurrentUser User user,
      @PathVariable Long caseId,
      @Valid @RequestBody CreateDocumentRequest request) {
    DocumentResponse result = documentService.createDraft(user, caseId, request);
    return ResponseEntity.ok(BaseResponse.success(result));
  }

  @Operation(
      summary = "[ 사용자 | 토큰 O | 내 문서 목록 조회 ]",
      description =
          """
            **Parameters**  \n
            caseId(선택), docType(선택), status(선택), isLatest(선택), page(0-base)/size/sort \n
            """)
  @GetMapping("/api/documents")
  public ResponseEntity<BaseResponse<OffsetPageResponse<DocumentResponse>>> getList(
      @CurrentUser User user,
      @RequestParam(required = false) Long caseId,
      @RequestParam(required = false) DocType docType,
      @RequestParam(required = false) DocumentStatus status,
      @RequestParam(required = false) Boolean isLatest,
      @PageableDefault(size = 20) Pageable pageable) {
    OffsetPageResponse<DocumentResponse> result =
        documentService.getList(user, caseId, docType, status, isLatest, pageable);
    return ResponseEntity.ok(BaseResponse.success(result));
  }

  @Operation(summary = "[ 사용자 | 토큰 O | 문서 상세 조회 ]", description = "content 포함 전체 필드를 반환합니다.")
  @GetMapping("/api/documents/{documentId}")
  public ResponseEntity<BaseResponse<DocumentDetailResponse>> getDetail(
      @CurrentUser User user, @PathVariable Long documentId) {
    DocumentDetailResponse result = documentService.getDetail(user, documentId);
    return ResponseEntity.ok(BaseResponse.success(result));
  }

  @Operation(
      summary = "[ 사용자 | 토큰 O | 문서 초안 수정 ]",
      description =
          """
            **Parameters**  \n
            title, content, writingRate(0~100) (null인 필드는 변경하지 않음) \n
            \n
            생성 로그가 RUNNING 상태면 409로 거절됩니다.
            """)
  @PatchMapping("/api/documents/{documentId}")
  public ResponseEntity<BaseResponse<DocumentResponse>> updateDraft(
      @CurrentUser User user,
      @PathVariable Long documentId,
      @Valid @RequestBody UpdateDocumentRequest request) {
    DocumentResponse result = documentService.updateDraft(user, documentId, request);
    return ResponseEntity.ok(BaseResponse.success(result));
  }

  @Operation(
      summary = "[ 사용자 | 토큰 O | AI 생성 결과 저장 ]",
      description =
          """
            **Parameters**  \n
            generatedText(RAG done.raw_text), generatedContent(RAG done.sections) \n
            \n
            프론트가 RAG SSE `done` 이벤트를 직접 소비한 뒤 그 결과를 이 API로 저장합니다(연동 A). \
            generatedText를 S3에 업로드해 fileUrl을 기록하고 generatedAt으로 AI 생성 완료를 확정합니다.
            """)
  @PostMapping("/api/documents/{documentId}/result")
  public ResponseEntity<BaseResponse<DocumentResponse>> saveResult(
      @CurrentUser User user,
      @PathVariable Long documentId,
      @Valid @RequestBody SaveGenerationResultRequest request) {
    DocumentResponse result = documentService.saveResult(user, documentId, request);
    return ResponseEntity.ok(BaseResponse.success(result));
  }

  @Operation(
      summary = "[ 사용자 | 토큰 O | 문서 제출 상태 변경 ]",
      description =
          """
            **Parameters**  \n
            status(DRAFT/SCHEDULED_TO_SUBMIT/SUBMITTED/NEEDS_REVISION) \n
            """)
  @PatchMapping("/api/documents/{documentId}/status")
  public ResponseEntity<BaseResponse<DocumentResponse>> updateStatus(
      @CurrentUser User user,
      @PathVariable Long documentId,
      @Valid @RequestBody UpdateDocumentStatusRequest request) {
    DocumentResponse result = documentService.updateStatus(user, documentId, request);
    return ResponseEntity.ok(BaseResponse.success(result));
  }

  @Operation(
      summary = "[ 사용자 | 토큰 O | 문서 다운로드 ]",
      description = "S3 presigned URL로 302 redirect 합니다. 아직 생성되지 않은 문서는 404를 반환합니다.")
  @GetMapping("/api/documents/{documentId}/download")
  public ResponseEntity<Void> download(@CurrentUser User user, @PathVariable Long documentId) {
    String presignedUrl = documentService.getDownloadUrl(user, documentId);
    return ResponseEntity.status(HttpStatus.FOUND)
        .headers(headers -> headers.setLocation(URI.create(presignedUrl)))
        .build();
  }

  @Operation(summary = "[ 사용자 | 토큰 O | 문서 삭제 ]", description = "생성 로그는 DB CASCADE로 함께 삭제됩니다.")
  @DeleteMapping("/api/documents/{documentId}")
  public ResponseEntity<BaseResponse<Void>> delete(
      @CurrentUser User user, @PathVariable Long documentId) {
    documentService.delete(user, documentId);
    return ResponseEntity.ok(BaseResponse.success(200, "문서가 삭제되었습니다.", null));
  }

  @Operation(summary = "[ 사용자 | 토큰 O | 생성 로그 조회 ]", description = "선택 기록된 생성 로그를 이력용으로 조회합니다.")
  @GetMapping("/api/documents/jobs/{jobId}")
  public ResponseEntity<BaseResponse<DocumentGenerationJobResponse>> getGenerationJob(
      @CurrentUser User user, @PathVariable Long jobId) {
    DocumentGenerationJobResponse result = documentService.getGenerationJob(user, jobId);
    return ResponseEntity.ok(BaseResponse.success(result));
  }
}
