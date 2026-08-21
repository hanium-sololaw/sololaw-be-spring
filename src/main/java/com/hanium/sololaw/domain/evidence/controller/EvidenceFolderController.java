/* 
 * Copyright (c) HANIUM SOLOLAW 
 */
package com.hanium.sololaw.domain.evidence.controller;

import java.util.List;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.hanium.sololaw.domain.evidence.dto.request.CreateEvidenceFolderRequest;
import com.hanium.sololaw.domain.evidence.dto.request.UpdateEvidenceFolderRequest;
import com.hanium.sololaw.domain.evidence.dto.response.EvidenceFolderResponse;
import com.hanium.sololaw.domain.evidence.service.EvidenceFolderService;
import com.hanium.sololaw.domain.user.entity.User;
import com.hanium.sololaw.global.common.BaseResponse;
import com.hanium.sololaw.global.security.annotation.CurrentUser;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequiredArgsConstructor
@Tag(name = "EvidenceFolder", description = "증거 폴더 관련 기능을 제공하는 API")
public class EvidenceFolderController {

  private final EvidenceFolderService evidenceFolderService;

  @Operation(
      summary = "[ 사용자 | 토큰 O | 증거 폴더 생성 ]",
      description = "caseId가 있으면 사건 소유자를 검증합니다. 미지정 시 사건 없는 폴더로 생성됩니다.")
  @PostMapping("/api/evidence-folders")
  public ResponseEntity<BaseResponse<EvidenceFolderResponse>> create(
      @CurrentUser User user, @Valid @RequestBody CreateEvidenceFolderRequest request) {
    EvidenceFolderResponse result = evidenceFolderService.create(user, request);
    return ResponseEntity.ok(BaseResponse.success(result));
  }

  @Operation(
      summary = "[ 사용자 | 토큰 O | 증거 폴더 목록 조회 ]",
      description = "caseId(선택)로 필터링합니다. 폴더별 증거 개수를 함께 반환합니다.")
  @GetMapping("/api/evidence-folders")
  public ResponseEntity<BaseResponse<List<EvidenceFolderResponse>>> getList(
      @CurrentUser User user, @RequestParam(required = false) Long caseId) {
    List<EvidenceFolderResponse> result = evidenceFolderService.getList(user, caseId);
    return ResponseEntity.ok(BaseResponse.success(result));
  }

  @Operation(
      summary = "[ 사용자 | 토큰 O | 증거 폴더 수정 ]",
      description = "name, folderType, tags (null인 필드는 변경하지 않음)")
  @PatchMapping("/api/evidence-folders/{folderId}")
  public ResponseEntity<BaseResponse<EvidenceFolderResponse>> update(
      @CurrentUser User user,
      @PathVariable Long folderId,
      @Valid @RequestBody UpdateEvidenceFolderRequest request) {
    EvidenceFolderResponse result = evidenceFolderService.update(user, folderId, request);
    return ResponseEntity.ok(BaseResponse.success(result));
  }

  @Operation(
      summary = "[ 사용자 | 토큰 O | 증거 폴더 삭제 ]",
      description = "소속 증거는 folder_id가 DB SET NULL로 보존되며 S3 파일은 삭제하지 않습니다.")
  @DeleteMapping("/api/evidence-folders/{folderId}")
  public ResponseEntity<BaseResponse<Void>> delete(
      @CurrentUser User user, @PathVariable Long folderId) {
    evidenceFolderService.delete(user, folderId);
    return ResponseEntity.ok(BaseResponse.success(200, "증거 폴더가 삭제되었습니다.", null));
  }
}
