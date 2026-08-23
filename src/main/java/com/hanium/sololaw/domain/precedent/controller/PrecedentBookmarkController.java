/* 
 * Copyright (c) HANIUM SOLOLAW 
 */
package com.hanium.sololaw.domain.precedent.controller;

import jakarta.validation.Valid;

import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.hanium.sololaw.domain.precedent.dto.request.CreatePrecedentBookmarkRequest;
import com.hanium.sololaw.domain.precedent.dto.response.PrecedentBookmarkResponse;
import com.hanium.sololaw.domain.precedent.service.PrecedentBookmarkService;
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
@Tag(name = "PrecedentBookmark", description = "판례 저장 관련 기능을 제공하는 API")
public class PrecedentBookmarkController {

  private final PrecedentBookmarkService precedentBookmarkService;

  @Operation(
      summary = "[ 사용자 | 토큰 O | 판례 저장 ]",
      description = "프론트가 RAG 검색 응답을 그대로 전달합니다. 이미 저장된 판례(동일 serialId)면 기존 행을 그대로 반환합니다(멱등).")
  @PostMapping("/api/precedent-bookmarks")
  public ResponseEntity<BaseResponse<PrecedentBookmarkResponse>> create(
      @CurrentUser User user, @Valid @RequestBody CreatePrecedentBookmarkRequest request) {
    PrecedentBookmarkResponse result = precedentBookmarkService.create(user, request);
    return ResponseEntity.ok(BaseResponse.success(result));
  }

  @Operation(summary = "[ 사용자 | 토큰 O | 저장 판례 목록 조회 ]", description = "page(0-base)/size/sort")
  @GetMapping("/api/precedent-bookmarks")
  public ResponseEntity<BaseResponse<OffsetPageResponse<PrecedentBookmarkResponse>>> getList(
      @CurrentUser User user, @PageableDefault(size = 20) Pageable pageable) {
    OffsetPageResponse<PrecedentBookmarkResponse> result =
        precedentBookmarkService.getList(user, pageable);
    return ResponseEntity.ok(BaseResponse.success(result));
  }

  @Operation(summary = "[ 사용자 | 토큰 O | 판례 저장 해제 ]")
  @DeleteMapping("/api/precedent-bookmarks/{bookmarkId}")
  public ResponseEntity<BaseResponse<Void>> delete(
      @CurrentUser User user, @PathVariable Long bookmarkId) {
    precedentBookmarkService.delete(user, bookmarkId);
    return ResponseEntity.ok(BaseResponse.success(200, "판례 저장이 해제되었습니다.", null));
  }
}
