/* 
 * Copyright (c) HANIUM SOLOLAW 
 */
package com.hanium.sololaw.domain.cases.controller;

import java.util.List;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hanium.sololaw.domain.cases.dto.request.CreatePartyRequest;
import com.hanium.sololaw.domain.cases.dto.request.UpdatePartyRequest;
import com.hanium.sololaw.domain.cases.dto.response.CasePartyResponse;
import com.hanium.sololaw.domain.cases.service.CasePartyService;
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
@RequestMapping("/api/cases/{caseId}/parties")
@Tag(name = "CaseParty", description = "사건 당사자 관련 기능을 제공하는 API")
public class CasePartyController {

  private final CasePartyService casePartyService;

  @Operation(
      summary = "[ 사용자 | 토큰 O | 당사자 목록 조회 ]",
      description = """
            공동소송 시 PLAINTIFF 행이 여러 개일 수 있습니다.
            """)
  @GetMapping
  public ResponseEntity<BaseResponse<List<CasePartyResponse>>> getParties(
      @CurrentUser User user, @PathVariable Long caseId) {
    List<CasePartyResponse> result = casePartyService.getParties(user, caseId);
    return ResponseEntity.ok(BaseResponse.success(result));
  }

  @Operation(
      summary = "[ 사용자 | 토큰 O | 당사자 추가 ]",
      description =
          """
            **Parameters**  \n
            partyRole, name \n
            \n
            "+ 원고 추가(공동소송)" 등 사건 생성 이후 추가 당사자를 등록할 때 사용합니다.
            """)
  @PostMapping
  public ResponseEntity<BaseResponse<CasePartyResponse>> addParty(
      @CurrentUser User user,
      @PathVariable Long caseId,
      @Valid @RequestBody CreatePartyRequest request) {
    CasePartyResponse result = casePartyService.addParty(user, caseId, request);
    return ResponseEntity.ok(BaseResponse.success(result));
  }

  @Operation(
      summary = "[ 사용자 | 토큰 O | 당사자 상세정보 수정 ]",
      description =
          """
            **Parameters**  \n
            residentNo(평문 전송, 서버가 AES 암호화 저장), address, phone (null인 필드는 변경하지 않음) \n
            \n
            문서 생성(02번) 소장 위저드의 당사자 정보 스텝에서 호출됩니다.
            """)
  @PatchMapping("/{partyId}")
  public ResponseEntity<BaseResponse<CasePartyResponse>> updatePartyDetails(
      @CurrentUser User user,
      @PathVariable Long caseId,
      @PathVariable Long partyId,
      @Valid @RequestBody UpdatePartyRequest request) {
    CasePartyResponse result = casePartyService.updatePartyDetails(user, caseId, partyId, request);
    return ResponseEntity.ok(BaseResponse.success(result));
  }

  @Operation(summary = "[ 사용자 | 토큰 O | 당사자 삭제 ]")
  @DeleteMapping("/{partyId}")
  public ResponseEntity<BaseResponse<Void>> deleteParty(
      @CurrentUser User user, @PathVariable Long caseId, @PathVariable Long partyId) {
    casePartyService.deleteParty(user, caseId, partyId);
    return ResponseEntity.ok(BaseResponse.success(200, "당사자가 삭제되었습니다.", null));
  }
}
