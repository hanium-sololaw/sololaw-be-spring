/* 
 * Copyright (c) HANIUM SOLOLAW 
 */
package com.hanium.sololaw.domain.payment.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hanium.sololaw.domain.payment.service.StripeWebhookService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Stripe가 직접 호출하는 웹훅 엔드포인트입니다. {@code /internal/**}과 달리 Stripe 서버(공인 인터넷)에서 도달해야 하므로 인프라 차단 대상이
 * 아닙니다 — 서명 검증({@code Stripe-Signature} 헤더)이 인증을 대신합니다.
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/webhooks/stripe")
@Tag(name = "Stripe Webhook", description = "Stripe 서버 전용 웹훅 — Stripe-Signature 헤더로 인증")
public class StripeWebhookController {

  private final StripeWebhookService stripeWebhookService;

  @Operation(
      summary = "[ Stripe 서버 전용 | Stripe-Signature 서명 인증 | 결제 이벤트 수신 ]",
      description =
          """
            **Parameters**  \n
            payload: Stripe가 보낸 원본 요청 바디(JSON, 서명 검증에 그대로 쓰이므로 가공하지 않는다) \n
            Stripe-Signature 헤더: 서명값 \n
            \n
            **Returns**  \n
            200: 처리 완료(이미 처리된 이벤트를 포함) \n
            400: 서명 검증 실패 \n
            \n
            결제창에서 결제는 성공했지만 프론트가 confirm API를 호출하지 못한 경우(리다이렉트 중 이탈 등)를 구제하는 안전망입니다. \
            confirm API와 같은 활성화 로직을 재사용하며, 이미 confirm으로 처리된 주문은 멱등하게 무시합니다.
            """)
  @PostMapping
  public ResponseEntity<Void> handle(
      @RequestBody String payload, @RequestHeader("Stripe-Signature") String signature) {
    log.debug("[StripeWebhookController] handle() - START");
    stripeWebhookService.handle(payload, signature);
    log.debug("[StripeWebhookController] handle() - END");
    return ResponseEntity.ok().build();
  }
}
