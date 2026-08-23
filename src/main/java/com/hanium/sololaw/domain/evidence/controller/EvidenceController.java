/* 
 * Copyright (c) HANIUM SOLOLAW 
 */
package com.hanium.sololaw.domain.evidence.controller;

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

import com.hanium.sololaw.domain.evidence.dto.request.CreateEvidenceRequest;
import com.hanium.sololaw.domain.evidence.dto.request.CreateEvidenceUploadUrlRequest;
import com.hanium.sololaw.domain.evidence.dto.request.UpdateEvidenceRequest;
import com.hanium.sololaw.domain.evidence.dto.request.UpdateEvidenceStatusRequest;
import com.hanium.sololaw.domain.evidence.dto.response.EvidenceResponse;
import com.hanium.sololaw.domain.evidence.dto.response.EvidenceUploadUrlResponse;
import com.hanium.sololaw.domain.evidence.entity.enums.EvidenceStatus;
import com.hanium.sololaw.domain.evidence.entity.enums.ExhibitParty;
import com.hanium.sololaw.domain.evidence.service.EvidenceService;
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
@Tag(name = "Evidence", description = "증거 관리 관련 기능을 제공하는 API")
public class EvidenceController {

  private final EvidenceService evidenceService;

  @Operation(
      summary = "[ 사용자 | 토큰 O | 증거 업로드용 presigned URL 발급 ]",
      description =
          """
            **Parameters**  \n
            caseId, fileName, contentType, fileSize \n
            \n
            프론트가 이 URL로 스토리지에 직접 PUT 업로드합니다. 용량 초과 시 413(STORAGE_QUOTA_EXCEEDED)을 반환합니다 \
            (최종 확정은 증거 등록 시점의 원자 UPDATE에서 이뤄집니다).
            """)
  @PostMapping("/api/evidence/upload-url")
  public ResponseEntity<BaseResponse<EvidenceUploadUrlResponse>> createUploadUrl(
      @CurrentUser User user, @Valid @RequestBody CreateEvidenceUploadUrlRequest request) {
    EvidenceUploadUrlResponse result = evidenceService.createUploadUrl(user, request);
    return ResponseEntity.ok(BaseResponse.success(result));
  }

  @Operation(
      summary = "[ 사용자 | 토큰 O | 증거 등록 ]",
      description =
          """
            **Parameters**  \n
            folderId(선택), partyType, exhibitNo(선택), fileName, fileUrl(upload-url 발급 시 받은 key), \
            fileSize, fileType(선택), proofPurpose(선택), description(선택), tags(선택), deadline(선택) \n
            \n
            presigned URL 업로드 완료 후 메타를 확정합니다. 저장 용량을 원자적으로 예약하며 초과 시 413을 반환합니다.
            """)
  @PostMapping("/api/cases/{caseId}/evidence")
  public ResponseEntity<BaseResponse<EvidenceResponse>> createEvidence(
      @CurrentUser User user,
      @PathVariable Long caseId,
      @Valid @RequestBody CreateEvidenceRequest request) {
    EvidenceResponse result = evidenceService.createEvidence(user, caseId, request);
    return ResponseEntity.ok(BaseResponse.success(result));
  }

  @Operation(
      summary = "[ 사용자 | 토큰 O | 사건별 증거 목록 조회 ]",
      description =
          """
            **Parameters**  \n
            status(선택), partyType(선택), folderId(선택), page(0-base)/size/sort \n
            """)
  @GetMapping("/api/cases/{caseId}/evidence")
  public ResponseEntity<BaseResponse<OffsetPageResponse<EvidenceResponse>>> getList(
      @CurrentUser User user,
      @PathVariable Long caseId,
      @RequestParam(required = false) EvidenceStatus status,
      @RequestParam(required = false) ExhibitParty partyType,
      @RequestParam(required = false) Long folderId,
      @PageableDefault(size = 20) Pageable pageable) {
    OffsetPageResponse<EvidenceResponse> result =
        evidenceService.getList(user, caseId, status, partyType, folderId, pageable);
    return ResponseEntity.ok(BaseResponse.success(result));
  }

  @Operation(
      summary = "[ 사용자 | 토큰 O | 다음 호증 번호 조회 ]",
      description =
          """
            **Parameters**  \n
            partyType(필수, GAP/EUL/BYEONG) \n
            \n
            사건·당사자지위별로 이미 저장된 증거 건수 + 1을 반환합니다. 준비서면·증거목록 AI 생성 시 \
            호증 번호가 사건 전체에서 이어지도록 시작 번호로 사용합니다.
            """)
  @GetMapping("/api/cases/{caseId}/evidence/next-exhibit-no")
  public ResponseEntity<BaseResponse<Integer>> getNextExhibitNo(
      @CurrentUser User user, @PathVariable Long caseId, @RequestParam ExhibitParty partyType) {
    int result = evidenceService.getNextExhibitNo(user, caseId, partyType);
    return ResponseEntity.ok(BaseResponse.success(result));
  }

  @Operation(summary = "[ 사용자 | 토큰 O | 증거 상세 조회 ]")
  @GetMapping("/api/evidence/{evidenceId}")
  public ResponseEntity<BaseResponse<EvidenceResponse>> getDetail(
      @CurrentUser User user, @PathVariable Long evidenceId) {
    EvidenceResponse result = evidenceService.getDetail(user, evidenceId);
    return ResponseEntity.ok(BaseResponse.success(result));
  }

  @Operation(
      summary = "[ 사용자 | 토큰 O | 증거 수정 ]",
      description =
          "exhibitNo, proofPurpose, description, tags, deadline (null인 필드는 변경하지 않음, 파일 자체는 교체 불가)")
  @PatchMapping("/api/evidence/{evidenceId}")
  public ResponseEntity<BaseResponse<EvidenceResponse>> update(
      @CurrentUser User user,
      @PathVariable Long evidenceId,
      @Valid @RequestBody UpdateEvidenceRequest request) {
    EvidenceResponse result = evidenceService.update(user, evidenceId, request);
    return ResponseEntity.ok(BaseResponse.success(result));
  }

  @Operation(
      summary = "[ 사용자 | 토큰 O | 증거 상태 변경 ]",
      description =
          "status(PENDING/NOT_SUBMITTED/SUBMITTED/NEEDS_SUPPLEMENT). SUBMITTED로 전이 시 submittedAt이 채워집니다.")
  @PatchMapping("/api/evidence/{evidenceId}/status")
  public ResponseEntity<BaseResponse<EvidenceResponse>> updateStatus(
      @CurrentUser User user,
      @PathVariable Long evidenceId,
      @Valid @RequestBody UpdateEvidenceStatusRequest request) {
    EvidenceResponse result = evidenceService.updateStatus(user, evidenceId, request);
    return ResponseEntity.ok(BaseResponse.success(result));
  }

  @Operation(
      summary = "[ 사용자 | 토큰 O | 증거 다운로드 ]",
      description = "S3 presigned URL로 302 redirect 합니다.")
  @GetMapping("/api/evidence/{evidenceId}/download")
  public ResponseEntity<Void> download(@CurrentUser User user, @PathVariable Long evidenceId) {
    String presignedUrl = evidenceService.getDownloadUrl(user, evidenceId);
    return ResponseEntity.status(HttpStatus.FOUND)
        .headers(headers -> headers.setLocation(URI.create(presignedUrl)))
        .build();
  }

  @Operation(summary = "[ 사용자 | 토큰 O | 증거 삭제 ]", description = "저장 용량을 반환하고 S3 실 파일도 함께 삭제합니다.")
  @DeleteMapping("/api/evidence/{evidenceId}")
  public ResponseEntity<BaseResponse<Void>> delete(
      @CurrentUser User user, @PathVariable Long evidenceId) {
    evidenceService.delete(user, evidenceId);
    return ResponseEntity.ok(BaseResponse.success(200, "증거가 삭제되었습니다.", null));
  }
}
