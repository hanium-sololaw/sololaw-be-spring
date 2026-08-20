/* 
 * Copyright (c) HANIUM SOLOLAW 
 */
package com.hanium.sololaw.domain.document.service;

import org.springframework.data.domain.Pageable;

import com.hanium.sololaw.domain.document.dto.request.CreateDocumentRequest;
import com.hanium.sololaw.domain.document.dto.request.SaveGenerationResultRequest;
import com.hanium.sololaw.domain.document.dto.request.UpdateDocumentRequest;
import com.hanium.sololaw.domain.document.dto.request.UpdateDocumentStatusRequest;
import com.hanium.sololaw.domain.document.dto.response.DocumentDetailResponse;
import com.hanium.sololaw.domain.document.dto.response.DocumentGenerationJobResponse;
import com.hanium.sololaw.domain.document.dto.response.DocumentResponse;
import com.hanium.sololaw.domain.document.entity.enums.DocType;
import com.hanium.sololaw.domain.document.entity.enums.DocumentStatus;
import com.hanium.sololaw.domain.user.entity.User;
import com.hanium.sololaw.global.common.OffsetPageResponse;

public interface DocumentService {

  /**
   * 사건에 문서 초안을 생성합니다. 같은 case+docType의 기존 최신본은 is_latest=false로 내립니다.
   *
   * @param user : 로그인 사용자
   * @param caseId : 소속 사건 ID
   * @param request : 문서 초안 생성 요청
   * @return : 생성된 DocumentResponse
   */
  DocumentResponse createDraft(User user, Long caseId, CreateDocumentRequest request);

  /**
   * 로그인 사용자의 문서 목록을 조회합니다.
   *
   * @param user : 로그인 사용자
   * @param caseId : 사건 필터(선택)
   * @param docType : 문서 유형 필터(선택)
   * @param status : 제출 상태 필터(선택)
   * @param isLatest : 최신본 필터(선택)
   * @param pageable : page(0-base)/size/sort 쿼리 파라미터 바인딩
   * @return : OffsetPageResponse<DocumentResponse>
   */
  OffsetPageResponse<DocumentResponse> getList(
      User user,
      Long caseId,
      DocType docType,
      DocumentStatus status,
      Boolean isLatest,
      Pageable pageable);

  /**
   * 문서 상세(content 포함)를 조회합니다.
   *
   * @param user : 로그인 사용자
   * @param documentId : 조회할 문서 ID
   * @return : 조회된 DocumentDetailResponse
   */
  DocumentDetailResponse getDetail(User user, Long documentId);

  /**
   * 문서 초안을 수정합니다. 생성 로그가 RUNNING 상태면 409로 거절합니다.
   *
   * @param user : 로그인 사용자
   * @param documentId : 수정할 문서 ID
   * @param request : 초안 수정 요청
   * @return : 수정된 DocumentResponse
   */
  DocumentResponse updateDraft(User user, Long documentId, UpdateDocumentRequest request);

  /**
   * AI 생성 결과를 저장합니다. generatedText를 S3에 업로드해 file_url을 기록하고 generated_at을 채웁니다.
   *
   * @param user : 로그인 사용자
   * @param documentId : 대상 문서 ID
   * @param request : AI 생성 결과 저장 요청
   * @return : 수정된 DocumentResponse
   */
  DocumentResponse saveResult(User user, Long documentId, SaveGenerationResultRequest request);

  /**
   * 문서 제출 상태를 변경합니다.
   *
   * @param user : 로그인 사용자
   * @param documentId : 상태를 변경할 문서 ID
   * @param request : 상태 변경 요청
   * @return : 수정된 DocumentResponse
   */
  DocumentResponse updateStatus(User user, Long documentId, UpdateDocumentStatusRequest request);

  /**
   * 문서 다운로드용 presigned URL을 발급합니다. 아직 생성되지 않은 문서는 404를 반환합니다.
   *
   * @param user : 로그인 사용자
   * @param documentId : 다운로드할 문서 ID
   * @return : presigned GET URL
   */
  String getDownloadUrl(User user, Long documentId);

  /**
   * 문서를 삭제합니다. document_generation_jobs는 DB CASCADE로 함께 삭제됩니다.
   *
   * @param user : 로그인 사용자
   * @param documentId : 삭제할 문서 ID
   */
  void delete(User user, Long documentId);

  /**
   * 문서 생성 로그를 조회합니다(선택·이력용).
   *
   * @param user : 로그인 사용자
   * @param jobId : 조회할 잡 ID
   * @return : 조회된 DocumentGenerationJobResponse
   */
  DocumentGenerationJobResponse getGenerationJob(User user, Long jobId);
}
