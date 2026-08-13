/* 
 * Copyright (c) HANIUM SOLOLAW 
 */
package com.hanium.sololaw.domain.subscription.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hanium.sololaw.domain.subscription.dto.response.SubscriptionResponse;
import com.hanium.sololaw.domain.subscription.service.SubscriptionService;
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
@RequestMapping("/api/subscriptions")
@Tag(name = "Subscription", description = "내 구독 관련 기능을 제공하는 API")
public class SubscriptionController {

  private final SubscriptionService subscriptionService;

  @Operation(
      summary = "[ 사용자 | 토큰 O | 내 구독 조회 ]",
      description =
          """
            **Returns**  \n
            plan, status, storageLimitBytes, usedStorageBytes, priceKrw, billingCycle, \
            nextBillingAt, canceledAt, startedAt, expiresAt \n
            \n
            구독 정보는 회원가입 시 FREE 플랜 기본값으로 생성되어 항상 존재합니다.
            """)
  @GetMapping("/me")
  public ResponseEntity<BaseResponse<SubscriptionResponse>> getMe(@CurrentUser User user) {
    SubscriptionResponse result = subscriptionService.getMySubscription(user);
    return ResponseEntity.ok(BaseResponse.success(result));
  }
}
