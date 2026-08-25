/* 
 * Copyright (c) HANIUM SOLOLAW 
 */
package com.hanium.sololaw.domain.payment.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.hanium.sololaw.domain.payment.entity.enums.SubscriptionType;
import com.hanium.sololaw.domain.payment.exception.PaymentErrorCode;
import com.hanium.sololaw.domain.payment.gateway.PaymentConfirmation;
import com.hanium.sololaw.domain.payment.gateway.PaymentGateway;
import com.hanium.sololaw.domain.payment.repository.PendingCheckout;
import com.hanium.sololaw.domain.payment.repository.PendingCheckoutRepository;
import com.hanium.sololaw.global.config.property.StripeProperties;
import com.hanium.sololaw.global.exception.CustomException;
import com.stripe.net.Webhook;

@ExtendWith(MockitoExtension.class)
class StripeWebhookServiceTest {

  private static final String WEBHOOK_SECRET = "whsec_test_secret";

  @Mock private PaymentGateway paymentGateway;
  @Mock private PendingCheckoutRepository pendingCheckoutRepository;
  @Mock private PendingPaymentConfirmer storageConfirmer;
  @Mock private PendingPaymentConfirmer precedentConfirmer;

  private StripeWebhookService stripeWebhookService;

  @BeforeEach
  void setUp() {
    StripeProperties properties =
        new StripeProperties(
            "sk_test",
            "pk_test",
            "https://example.com/success",
            "https://example.com/cancel",
            WEBHOOK_SECRET);
    when(storageConfirmer.getSubscriptionType()).thenReturn(SubscriptionType.STORAGE);
    when(precedentConfirmer.getSubscriptionType()).thenReturn(SubscriptionType.PRECEDENT_SEARCH);
    stripeWebhookService =
        new StripeWebhookService(
            properties,
            paymentGateway,
            pendingCheckoutRepository,
            List.of(storageConfirmer, precedentConfirmer));
    stripeWebhookService.init();
  }

  private String sign(String payload) throws Exception {
    return Webhook.Signature.generateSignatureHeader(payload, WEBHOOK_SECRET);
  }

  private String checkoutSessionCompletedPayload(String orderId, long amount) {
    return """
        {
          "id": "evt_test_1",
          "object": "event",
          "api_version": "2026-08-12",
          "type": "checkout.session.completed",
          "data": {
            "object": {
              "id": "cs_test_abc123",
              "object": "checkout.session",
              "client_reference_id": "%s",
              "payment_status": "paid",
              "amount_total": %d,
              "payment_intent": "pi_test_123"
            }
          }
        }
        """
        .formatted(orderId, amount);
  }

  private String checkoutSessionAsyncFailedPayload(String orderId) {
    return """
        {
          "id": "evt_test_2",
          "object": "event",
          "api_version": "2026-08-12",
          "type": "checkout.session.async_payment_failed",
          "data": {
            "object": {
              "id": "cs_test_abc123",
              "object": "checkout.session",
              "client_reference_id": "%s"
            }
          }
        }
        """
        .formatted(orderId);
  }

  @Test
  void handle_throwsInvalidSignature_whenSignatureIsWrong() {
    String payload = checkoutSessionCompletedPayload("SUB-1", 12900);

    assertThatThrownBy(() -> stripeWebhookService.handle(payload, "t=1,v1=deadbeef"))
        .isInstanceOf(CustomException.class)
        .extracting(e -> ((CustomException) e).getErrorCode())
        .isEqualTo(PaymentErrorCode.INVALID_WEBHOOK_SIGNATURE);
  }

  @Test
  void handle_activatesStorageSubscription_whenCheckoutSessionCompleted() throws Exception {
    String payload = checkoutSessionCompletedPayload("SUB-1", 12900);
    PendingCheckout pending = new PendingCheckout(1L, SubscriptionType.STORAGE, "STANDARD", 12900);
    when(pendingCheckoutRepository.find("SUB-1")).thenReturn(Optional.of(pending));
    when(paymentGateway.confirm("cs_test_abc123", "SUB-1", 12900))
        .thenReturn(new PaymentConfirmation("pi_test_123", 12900, LocalDateTime.now(), null));

    stripeWebhookService.handle(payload, sign(payload));

    verify(storageConfirmer)
        .confirmPending(eq(pending), eq("SUB-1"), any(PaymentConfirmation.class));
    verify(precedentConfirmer, never()).confirmPending(any(), any(), any());
  }

  @Test
  void handle_activatesPrecedentSubscription_whenCheckoutSessionCompleted() throws Exception {
    String payload = checkoutSessionCompletedPayload("PSUB-1", 14900);
    PendingCheckout pending =
        new PendingCheckout(1L, SubscriptionType.PRECEDENT_SEARCH, "PREMIUM", 14900);
    when(pendingCheckoutRepository.find("PSUB-1")).thenReturn(Optional.of(pending));
    when(paymentGateway.confirm("cs_test_abc123", "PSUB-1", 14900))
        .thenReturn(new PaymentConfirmation("pi_test_456", 14900, LocalDateTime.now(), null));

    stripeWebhookService.handle(payload, sign(payload));

    verify(precedentConfirmer)
        .confirmPending(eq(pending), eq("PSUB-1"), any(PaymentConfirmation.class));
    verify(storageConfirmer, never()).confirmPending(any(), any(), any());
  }

  @Test
  void handle_isIdempotent_whenPendingSessionAlreadyConsumed() throws Exception {
    String payload = checkoutSessionCompletedPayload("SUB-1", 12900);
    when(pendingCheckoutRepository.find("SUB-1")).thenReturn(Optional.empty());

    stripeWebhookService.handle(payload, sign(payload));

    verify(storageConfirmer, never()).confirmPending(any(), any(), any());
    verify(precedentConfirmer, never()).confirmPending(any(), any(), any());
    verify(paymentGateway, never()).confirm(any(), any(), org.mockito.ArgumentMatchers.anyLong());
  }

  @Test
  void handle_deletesPendingSession_whenAsyncPaymentFailed() throws Exception {
    String payload = checkoutSessionAsyncFailedPayload("SUB-1");

    stripeWebhookService.handle(payload, sign(payload));

    verify(pendingCheckoutRepository).delete("SUB-1");
    verify(storageConfirmer, never()).confirmPending(any(), any(), any());
  }

  @Test
  void handle_ignoresUnhandledEventType() throws Exception {
    String payload =
        """
        {
          "id": "evt_test_3",
          "object": "event",
          "api_version": "2026-08-12",
          "type": "invoice.paid",
          "data": { "object": { "id": "in_test_1", "object": "invoice" } }
        }
        """;

    stripeWebhookService.handle(payload, sign(payload));

    verify(pendingCheckoutRepository, never()).find(any());
    verify(pendingCheckoutRepository, never()).delete(any());
  }
}
