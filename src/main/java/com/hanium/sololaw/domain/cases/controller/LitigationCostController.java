/* 
 * Copyright (c) HANIUM SOLOLAW 
 */
package com.hanium.sololaw.domain.cases.controller;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hanium.sololaw.domain.cases.dto.request.CalculateLitigationCostRequest;
import com.hanium.sololaw.domain.cases.dto.response.LitigationCostResponse;
import com.hanium.sololaw.domain.cases.service.LitigationCostService;
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
@RequestMapping("/api/litigation-costs")
@Tag(name = "LitigationCost", description = "인지대·송달료(소송비용) 계산 API — 특정 사건과 무관한 독립형 계산기")
public class LitigationCostController {

  private final LitigationCostService litigationCostService;

  @Operation(
      summary = "[ 사용자 | 토큰 O | 인지대·송달료 계산 ]",
      description =
          """
            **Parameters**  \n
            claimAmount(청구 금액), plaintiffCount(원고 수), defendantCount(피고 수), \
            filingMethod(ELECTRONIC/PAPER), instance(FIRST/APPEAL/SUPREME) \n
            \n
            **Returns**  \n
            claimAmount, isSmallClaim(소액사건 여부), isElectronicFiling, instance, \
            stampFee(인지액), deliveryFee(송달료), totalCost, partyCount, deliveryCount, disclaimer \n
            \n
            사건 저장 여부와 무관하게 입력값만으로 민사소송 등 인지법 공식에 따라 계산합니다. \
            법원이 실제 접수 시 다시 계산하는 참고용 수치입니다.
            """)
  @PostMapping("/calculate")
  public ResponseEntity<BaseResponse<LitigationCostResponse>> calculate(
      @CurrentUser User user, @Valid @RequestBody CalculateLitigationCostRequest request) {
    LitigationCostResponse result = litigationCostService.calculate(request);
    return ResponseEntity.ok(BaseResponse.success(result));
  }
}
