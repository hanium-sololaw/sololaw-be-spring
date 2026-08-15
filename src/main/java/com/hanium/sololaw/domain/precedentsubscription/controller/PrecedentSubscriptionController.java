/* 
 * Copyright (c) HANIUM SOLOLAW 
 */
package com.hanium.sololaw.domain.precedentsubscription.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hanium.sololaw.domain.precedentsubscription.dto.response.PrecedentSubscriptionResponse;
import com.hanium.sololaw.domain.precedentsubscription.service.PrecedentSubscriptionService;
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
@RequestMapping("/api/precedent-subscriptions")
@Tag(name = "PrecedentSubscription", description = "내 판례검색 구독 관련 기능을 제공하는 API")
public class PrecedentSubscriptionController {

  private final PrecedentSubscriptionService precedentSubscriptionService;

  @Operation(
      summary = "[ 사용자 | 토큰 O | 내 판례검색 구독 조회 ]",
      description =
          """
            **Returns**  \n
            plan, status, priceKrw, billingCycle, nextBillingAt, canceledAt, startedAt, expiresAt \n
            \n
            구독 정보는 회원가입 시 FREE 플랜 기본값으로 생성되어 항상 존재합니다. 저장공간 구독(subscriptions)과 완전히 \
            독립적입니다.
            """)
  @GetMapping("/me")
  public ResponseEntity<BaseResponse<PrecedentSubscriptionResponse>> getMe(@CurrentUser User user) {
    PrecedentSubscriptionResponse result =
        precedentSubscriptionService.getMyPrecedentSubscription(user);
    return ResponseEntity.ok(BaseResponse.success(result));
  }
}
