/* 
 * Copyright (c) HANIUM SOLOLAW 
 */
package com.hanium.sololaw.domain.cases.controller;

import jakarta.validation.Valid;

import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.hanium.sololaw.domain.cases.dto.request.CreateCaseRequest;
import com.hanium.sololaw.domain.cases.dto.request.UpdateCaseRequest;
import com.hanium.sololaw.domain.cases.dto.request.UpdateCaseStatusRequest;
import com.hanium.sololaw.domain.cases.dto.response.CaseDetailResponse;
import com.hanium.sololaw.domain.cases.dto.response.CaseResponse;
import com.hanium.sololaw.domain.cases.entity.enums.CaseStatus;
import com.hanium.sololaw.domain.cases.entity.enums.CaseType;
import com.hanium.sololaw.domain.cases.service.CaseService;
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
@RequestMapping("/api/cases")
@Tag(name = "Case", description = "사건 관리 관련 기능을 제공하는 API")
public class CaseController {

  private final CaseService caseService;

  @Operation(
      summary = "[ 사용자 | 토큰 O | 사건 생성 ]",
      description =
          """
            **Parameters**  \n
            title, caseType(선택), opponentName, claimAmount(선택), court(선택), caseNumber(선택), startingStage(선택) \n
            \n
            **Returns**  \n
            생성된 사건. opponentName으로 피고, 로그인 사용자 이름으로 원고(나) 당사자가 함께 자동 생성되고, \
            caseType이 미정이 아니면 표준 절차 6단계가 startingStage 기준으로 자동 시드됩니다.
            """)
  @PostMapping
  public ResponseEntity<BaseResponse<CaseResponse>> createCase(
      @CurrentUser User user, @Valid @RequestBody CreateCaseRequest request) {
    CaseResponse result = caseService.createCase(user, request);
    return ResponseEntity.ok(BaseResponse.success(result));
  }

  @Operation(
      summary = "[ 사용자 | 토큰 O | 내 사건 목록 조회 ]",
      description =
          """
            **Parameters**  \n
            status(선택), caseType(선택), page(0-base)/size/sort \n
            """)
  @GetMapping
  public ResponseEntity<BaseResponse<OffsetPageResponse<CaseResponse>>> getCaseList(
      @CurrentUser User user,
      @RequestParam(required = false) CaseStatus status,
      @RequestParam(required = false) CaseType caseType,
      @PageableDefault(size = 20) Pageable pageable) {
    OffsetPageResponse<CaseResponse> result =
        caseService.getCaseList(user, status, caseType, pageable);
    return ResponseEntity.ok(BaseResponse.success(result));
  }

  @Operation(
      summary = "[ 사용자 | 토큰 O | 사건 상세 조회 ]",
      description =
          """
            **Returns**  \n
            사건 정보 + 당사자 요약 + 진행률 + 문서/증빙/일정/최근활동 집계, 최근활동은 08번 activity_logs 미구현으로 현재 0 고정
            """)
  @GetMapping("/{caseId}")
  public ResponseEntity<BaseResponse<CaseDetailResponse>> getCaseDetail(
      @CurrentUser User user, @PathVariable Long caseId) {
    CaseDetailResponse result = caseService.getCaseDetail(user, caseId);
    return ResponseEntity.ok(BaseResponse.success(result));
  }

  @Operation(
      summary = "[ 사용자 | 토큰 O | 사건 수정 ]",
      description =
          """
            **Parameters**  \n
            title, caseType, claimAmount, court, caseNumber, filingMethod(선택, 전자소송 또는 종이 제출) (null인 필드는 변경하지 않음) \n
            \n
            caseType이 미정(null)에서 값으로 처음 채워지면 그 시점에 표준 절차 6단계가 1단계부터 자동 시드됩니다.
            """)
  @PatchMapping("/{caseId}")
  public ResponseEntity<BaseResponse<CaseResponse>> updateCase(
      @CurrentUser User user,
      @PathVariable Long caseId,
      @Valid @RequestBody UpdateCaseRequest request) {
    CaseResponse result = caseService.updateCase(user, caseId, request);
    return ResponseEntity.ok(BaseResponse.success(result));
  }

  @Operation(
      summary = "[ 사용자 | 토큰 O | 사건 상태 변경 ]",
      description =
          """
            **Parameters**  \n
            status, reason(정정 사유, 5자 이상) \n
            """)
  @PatchMapping("/{caseId}/status")
  public ResponseEntity<BaseResponse<CaseResponse>> updateCaseStatus(
      @CurrentUser User user,
      @PathVariable Long caseId,
      @Valid @RequestBody UpdateCaseStatusRequest request) {
    CaseResponse result = caseService.updateCaseStatus(user, caseId, request);
    return ResponseEntity.ok(BaseResponse.success(result));
  }

  @Operation(
      summary = "[ 사용자 | 토큰 O | 사건 삭제 ]",
      description = """
            당사자/절차 단계/할 일은 DB CASCADE로 함께 삭제됩니다.
            """)
  @DeleteMapping("/{caseId}")
  public ResponseEntity<BaseResponse<Void>> deleteCase(
      @CurrentUser User user, @PathVariable Long caseId) {
    caseService.deleteCase(user, caseId);
    return ResponseEntity.ok(BaseResponse.success(200, "사건이 삭제되었습니다.", null));
  }
}
