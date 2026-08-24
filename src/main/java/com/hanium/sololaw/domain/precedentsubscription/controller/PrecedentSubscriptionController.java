/* 
 * Copyright (c) HANIUM SOLOLAW 
 */
package com.hanium.sololaw.domain.precedentsubscription.controller;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hanium.sololaw.domain.precedentsubscription.dto.request.CheckoutRequest;
import com.hanium.sololaw.domain.precedentsubscription.dto.request.ConfirmRequest;
import com.hanium.sololaw.domain.precedentsubscription.dto.response.CheckoutResponse;
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

  @Operation(
      summary = "[ 사용자 | 토큰 O | 결제 시작 ]",
      description =
          """
            **Parameters**  \n
            plan: 구독할 플랜(PREMIUM — FREE는 결제 대상이 아님) \n
            \n
            **Returns**  \n
            orderId, amount, orderName, customerEmail, customerName, \
            clientKey(토스 위젯 초기화용), redirectUrl \n
            \n
            결제 대기 세션을 서버(Redis)에 30분 TTL로 저장합니다. 프론트는 이 응답으로 결제창을 띄우고, \
            완료 후 confirm API를 호출해야 실제로 구독이 반영됩니다.
            """)
  @PostMapping("/checkout")
  public ResponseEntity<BaseResponse<CheckoutResponse>> checkout(
      @CurrentUser User user, @Valid @RequestBody CheckoutRequest request) {
    CheckoutResponse result = precedentSubscriptionService.checkout(user, request);
    return ResponseEntity.ok(BaseResponse.success(result));
  }

  @Operation(
      summary = "[ 사용자 | 토큰 O | 결제 승인 ]",
      description =
          """
            **Parameters**  \n
            paymentKey: 결제창 완료 후 토스가 프론트에 전달한 결제 키 \n
            orderId: checkout에서 발급받은 주문 ID \n
            amount: 결제 금액(원) \n
            \n
            **Returns**  \n
            plan, status, priceKrw, billingCycle, nextBillingAt, canceledAt, startedAt, expiresAt \n
            \n
            토스 confirm API로 결제를 재검증한 뒤 구독을 활성화합니다. checkout 없이 호출하거나 \
            금액이 다르면 400으로 거부됩니다.
            """)
  @PostMapping("/confirm")
  public ResponseEntity<BaseResponse<PrecedentSubscriptionResponse>> confirm(
      @CurrentUser User user, @Valid @RequestBody ConfirmRequest request) {
    PrecedentSubscriptionResponse result = precedentSubscriptionService.confirm(user, request);
    return ResponseEntity.ok(BaseResponse.success(result));
  }

  @Operation(
      summary = "[ 사용자 | 토큰 O | 구독 해지 ]",
      description =
          """
            **Returns**  \n
            plan, status, priceKrw, billingCycle, nextBillingAt, canceledAt, startedAt, expiresAt \n
            \n
            다음 결제 주기부터 갱신되지 않도록 예약합니다. 현재 플랜은 만료 전까지 그대로 유지됩니다.
            """)
  @PostMapping("/cancel")
  public ResponseEntity<BaseResponse<PrecedentSubscriptionResponse>> cancel(
      @CurrentUser User user) {
    PrecedentSubscriptionResponse result = precedentSubscriptionService.cancel(user);
    return ResponseEntity.ok(BaseResponse.success(result));
  }
}
