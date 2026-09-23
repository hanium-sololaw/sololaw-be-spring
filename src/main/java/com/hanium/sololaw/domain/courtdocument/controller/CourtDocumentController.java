/* 
 * Copyright (c) HANIUM SOLOLAW 
 */
package com.hanium.sololaw.domain.courtdocument.controller;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.hanium.sololaw.domain.courtdocument.dto.response.CourtDocumentAnalysisResponse;
import com.hanium.sololaw.domain.courtdocument.service.CourtDocumentService;
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
@Tag(name = "CourtDocument", description = "법원 문서 자동 등록 관련 기능을 제공하는 API")
public class CourtDocumentController {

  private final CourtDocumentService courtDocumentService;

  @Operation(
      summary = "[ 사용자 | 토큰 O | 법원 문서 분석 ]",
      description =
          "PDF/JPG/PNG 업로드 시 AI가 문서 종류(기일통지서/보정명령/소장부본송달 등)를 인식해 사건번호·사건명·"
              + "변론기일 또는 제출기한·법원·재판부를 추출한다. 저장은 하지 않으며, 반환된 값을 확인·수정한 뒤 "
              + "별도로 POST /api/schedules를 호출해 확정한다.")
  @PostMapping(
      path = "/api/schedules/court-document/analyze",
      consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<BaseResponse<CourtDocumentAnalysisResponse>> analyze(
      @CurrentUser User user, @RequestParam("file") MultipartFile file) {
    CourtDocumentAnalysisResponse result = courtDocumentService.analyze(user, file);
    return ResponseEntity.ok(BaseResponse.success(result));
  }
}
