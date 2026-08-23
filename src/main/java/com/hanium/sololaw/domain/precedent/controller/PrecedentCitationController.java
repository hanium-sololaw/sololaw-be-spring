/* 
 * Copyright (c) HANIUM SOLOLAW 
 */
package com.hanium.sololaw.domain.precedent.controller;

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

import com.hanium.sololaw.domain.precedent.dto.request.CreatePrecedentCitationRequest;
import com.hanium.sololaw.domain.precedent.dto.request.LinkDocumentRequest;
import com.hanium.sololaw.domain.precedent.dto.response.PrecedentCitationResponse;
import com.hanium.sololaw.domain.precedent.service.PrecedentCitationService;
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
@Tag(name = "PrecedentCitation", description = "판례 인용 관련 기능을 제공하는 API")
public class PrecedentCitationController {

  private final PrecedentCitationService precedentCitationService;

  @Operation(
      summary = "[ 사용자 | 토큰 O | 판례 인용 추가 ]",
      description = "caseId 필수, documentId는 선택(문서 생성이 확정되면 이후 PATCH로 연결 가능)")
  @PostMapping("/api/precedent-citations")
  public ResponseEntity<BaseResponse<PrecedentCitationResponse>> create(
      @CurrentUser User user, @Valid @RequestBody CreatePrecedentCitationRequest request) {
    PrecedentCitationResponse result = precedentCitationService.create(user, request);
    return ResponseEntity.ok(BaseResponse.success(result));
  }

  @Operation(summary = "[ 사용자 | 토큰 O | 내 인용 목록 조회 ]", description = "caseId, documentId 전부 선택")
  @GetMapping("/api/precedent-citations")
  public ResponseEntity<BaseResponse<List<PrecedentCitationResponse>>> getList(
      @CurrentUser User user,
      @RequestParam(required = false) Long caseId,
      @RequestParam(required = false) Long documentId) {
    List<PrecedentCitationResponse> result =
        precedentCitationService.getList(user, caseId, documentId);
    return ResponseEntity.ok(BaseResponse.success(result));
  }

  @Operation(summary = "[ 사용자 | 토큰 O | 인용을 문서에 연결 ]", description = "documentId 필수")
  @PatchMapping("/api/precedent-citations/{citationId}")
  public ResponseEntity<BaseResponse<PrecedentCitationResponse>> linkDocument(
      @CurrentUser User user,
      @PathVariable Long citationId,
      @Valid @RequestBody LinkDocumentRequest request) {
    PrecedentCitationResponse result =
        precedentCitationService.linkDocument(user, citationId, request);
    return ResponseEntity.ok(BaseResponse.success(result));
  }

  @Operation(summary = "[ 사용자 | 토큰 O | 판례 인용 삭제 ]")
  @DeleteMapping("/api/precedent-citations/{citationId}")
  public ResponseEntity<BaseResponse<Void>> delete(
      @CurrentUser User user, @PathVariable Long citationId) {
    precedentCitationService.delete(user, citationId);
    return ResponseEntity.ok(BaseResponse.success(200, "판례 인용이 삭제되었습니다.", null));
  }
}
