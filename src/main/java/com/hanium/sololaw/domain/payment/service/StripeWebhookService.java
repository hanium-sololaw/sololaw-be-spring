/* 
 * Copyright (c) HANIUM SOLOLAW 
 */
package com.hanium.sololaw.domain.payment.service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import jakarta.annotation.PostConstruct;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hanium.sololaw.domain.payment.entity.enums.SubscriptionType;
import com.hanium.sololaw.domain.payment.exception.PaymentErrorCode;
import com.hanium.sololaw.domain.payment.gateway.PaymentConfirmation;
import com.hanium.sololaw.domain.payment.gateway.PaymentGateway;
import com.hanium.sololaw.domain.payment.repository.PendingCheckout;
import com.hanium.sololaw.domain.payment.repository.PendingCheckoutRepository;
import com.hanium.sololaw.global.config.property.StripeProperties;
import com.hanium.sololaw.global.exception.CustomException;
import com.stripe.exception.EventDataObjectDeserializationException;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Stripe 웹훅 처리. checkout API의 confirm()과 같은 활성화 로직({@link PendingPaymentConfirmer})을 재사용해, 결제창에서
 * 결제는 성공했는데 프론트가 confirm을 호출하지 못한 경우(리다이렉트 중 이탈, 네트워크 끊김 등)를 구제하는 안전망 역할만 한다.
 *
 * <p>이미 프론트의 confirm 호출로 대기 세션이 지워진 정상 케이스에는 아무 것도 하지 않고 조용히 끝난다(멱등 처리) — Stripe는 2xx가 아니면 재시도하므로,
 * 처리할 게 없는 이벤트도 반드시 정상 응답해야 한다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class StripeWebhookService {

  private static final String EVENT_CHECKOUT_COMPLETED = "checkout.session.completed";
  private static final String EVENT_ASYNC_PAYMENT_SUCCEEDED =
      "checkout.session.async_payment_succeeded";
  private static final String EVENT_ASYNC_PAYMENT_FAILED = "checkout.session.async_payment_failed";

  private final StripeProperties stripeProperties;
  private final PaymentGateway paymentGateway;
  private final PendingCheckoutRepository pendingCheckoutRepository;
  private final List<PendingPaymentConfirmer> confirmers;

  private Map<SubscriptionType, PendingPaymentConfirmer> confirmersByType;

  @PostConstruct
  void init() {
    confirmersByType =
        confirmers.stream()
            .collect(
                Collectors.toMap(
                    PendingPaymentConfirmer::getSubscriptionType, Function.identity()));
  }

  public void handle(String payload, String signatureHeader) {
    Event event;
    try {
      event = Webhook.constructEvent(payload, signatureHeader, stripeProperties.getWebhookSecret());
    } catch (SignatureVerificationException e) {
      log.warn("[StripeWebhookService] handle() - 서명 검증 실패: {}", e.getMessage());
      throw new CustomException(PaymentErrorCode.INVALID_WEBHOOK_SIGNATURE);
    }

    log.debug("[StripeWebhookService] handle() - eventType: {}", event.getType());
    switch (event.getType()) {
      case EVENT_CHECKOUT_COMPLETED, EVENT_ASYNC_PAYMENT_SUCCEEDED -> handleCompleted(event);
      case EVENT_ASYNC_PAYMENT_FAILED -> handleFailed(event);
      default -> log.debug("[StripeWebhookService] handle() - 처리 대상 아닌 이벤트: {}", event.getType());
    }
  }

  @Transactional
  void handleCompleted(Event event) {
    Session session = extractSession(event);
    if (session == null || session.getClientReferenceId() == null) {
      return;
    }
    String orderId = session.getClientReferenceId();

    Optional<PendingCheckout> maybePending = pendingCheckoutRepository.find(orderId);
    if (maybePending.isEmpty()) {
      log.debug(
          "[StripeWebhookService] handleCompleted() - 이미 처리됐거나 만료된 세션(멱등) | orderId: {}", orderId);
      return;
    }
    PendingCheckout pending = maybePending.get();

    PendingPaymentConfirmer confirmer = confirmersByType.get(pending.subscriptionType());
    if (confirmer == null) {
      log.error(
          "[StripeWebhookService] handleCompleted() - 알 수 없는 subscriptionType: {}",
          pending.subscriptionType());
      return;
    }

    /*
       웹훅 페이로드를 그대로 신뢰하지 않고, confirm API와 동일하게 PG 서버에 다시 확인한 뒤 활성화한다.
    */
    PaymentConfirmation confirmation =
        paymentGateway.confirm(session.getId(), orderId, pending.amount());
    confirmer.confirmPending(pending, orderId, confirmation);

    log.info(
        "[StripeWebhookService] handleCompleted() - 웹훅으로 구독 활성화 | orderId: {}, subscriptionType: {}",
        orderId,
        pending.subscriptionType());
  }

  void handleFailed(Event event) {
    Session session = extractSession(event);
    if (session == null || session.getClientReferenceId() == null) {
      return;
    }
    String orderId = session.getClientReferenceId();
    pendingCheckoutRepository.delete(orderId);
    log.info("[StripeWebhookService] handleFailed() - 결제 실패로 대기 세션 정리 | orderId: {}", orderId);
  }

  /**
   * {@code getObject()}는 이벤트의 api_version이 SDK 고정 버전과 정확히 일치하지 않으면 빈 값을 돌려준다(계정 기본 API 버전과 SDK가 다른
   * 흔한 경우에도 그렇다). 여기서는 세션에서 clientReferenceId·id만 꺼내 쓰고 금액·상태는 paymentGateway.confirm()으로 별도
   * 재검증하므로, 버전 호환성 검사를 건너뛰는 deserializeUnsafe()를 써도 안전하다.
   */
  private Session extractSession(Event event) {
    try {
      return (Session) event.getDataObjectDeserializer().deserializeUnsafe();
    } catch (EventDataObjectDeserializationException e) {
      log.warn(
          "[StripeWebhookService] extractSession() - 역직렬화 실패 | eventType: {}, error: {}",
          event.getType(),
          e.getMessage());
      return null;
    }
  }
}
