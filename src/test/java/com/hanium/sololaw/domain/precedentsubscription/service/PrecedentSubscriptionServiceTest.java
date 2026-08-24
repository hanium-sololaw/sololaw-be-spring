/* 
 * Copyright (c) HANIUM SOLOLAW 
 */
package com.hanium.sololaw.domain.precedentsubscription.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.hanium.sololaw.domain.payment.entity.Payment;
import com.hanium.sololaw.domain.payment.entity.enums.PaymentProvider;
import com.hanium.sololaw.domain.payment.entity.enums.SubscriptionType;
import com.hanium.sololaw.domain.payment.gateway.PaymentCheckoutCommand;
import com.hanium.sololaw.domain.payment.gateway.PaymentCheckoutInfo;
import com.hanium.sololaw.domain.payment.gateway.PaymentConfirmation;
import com.hanium.sololaw.domain.payment.gateway.PaymentGateway;
import com.hanium.sololaw.domain.payment.repository.PaymentRepository;
import com.hanium.sololaw.domain.payment.repository.PendingCheckout;
import com.hanium.sololaw.domain.payment.repository.PendingCheckoutRepository;
import com.hanium.sololaw.domain.precedentsubscription.dto.request.CheckoutRequest;
import com.hanium.sololaw.domain.precedentsubscription.dto.request.ConfirmRequest;
import com.hanium.sololaw.domain.precedentsubscription.dto.response.CheckoutResponse;
import com.hanium.sololaw.domain.precedentsubscription.dto.response.PrecedentSubscriptionResponse;
import com.hanium.sololaw.domain.precedentsubscription.entity.PrecedentSubscription;
import com.hanium.sololaw.domain.precedentsubscription.entity.enums.PrecedentSearchPlan;
import com.hanium.sololaw.domain.precedentsubscription.exception.PrecedentSubscriptionErrorCode;
import com.hanium.sololaw.domain.precedentsubscription.mapper.PrecedentSubscriptionMapper;
import com.hanium.sololaw.domain.precedentsubscription.repository.PrecedentSubscriptionRepository;
import com.hanium.sololaw.domain.subscription.entity.enums.SubscriptionStatus;
import com.hanium.sololaw.domain.user.entity.User;
import com.hanium.sololaw.global.exception.CustomException;

@ExtendWith(MockitoExtension.class)
class PrecedentSubscriptionServiceTest {

  @Mock private PrecedentSubscriptionRepository precedentSubscriptionRepository;
  @Mock private PrecedentSubscriptionMapper precedentSubscriptionMapper;
  @Mock private PaymentGateway paymentGateway;
  @Mock private PaymentRepository paymentRepository;
  @Mock private PendingCheckoutRepository pendingCheckoutRepository;

  @InjectMocks private PrecedentSubscriptionServiceImpl precedentSubscriptionService;

  @Test
  void getMyPrecedentSubscription_returnsMappedResponse_whenFound() {
    User user = User.builder().id(1L).build();
    PrecedentSubscription precedentSubscription =
        PrecedentSubscription.builder()
            .id(10L)
            .userId(1L)
            .plan(PrecedentSearchPlan.FREE)
            .status(SubscriptionStatus.ACTIVE)
            .build();
    PrecedentSubscriptionResponse expected =
        PrecedentSubscriptionResponse.builder()
            .plan(PrecedentSearchPlan.FREE)
            .status(SubscriptionStatus.ACTIVE)
            .build();
    when(precedentSubscriptionRepository.findByUserId(1L))
        .thenReturn(Optional.of(precedentSubscription));
    when(precedentSubscriptionMapper.toResponse(precedentSubscription)).thenReturn(expected);

    PrecedentSubscriptionResponse result =
        precedentSubscriptionService.getMyPrecedentSubscription(user);

    assertThat(result).isEqualTo(expected);
  }

  @Test
  void getMyPrecedentSubscription_throwsNotFound_whenMissing() {
    User user = User.builder().id(1L).build();
    when(precedentSubscriptionRepository.findByUserId(1L)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> precedentSubscriptionService.getMyPrecedentSubscription(user))
        .isInstanceOf(CustomException.class)
        .extracting(e -> ((CustomException) e).getErrorCode())
        .isEqualTo(PrecedentSubscriptionErrorCode.PRECEDENT_SUBSCRIPTION_NOT_FOUND);
  }

  @Test
  void checkout_throwsFreePlanError_whenTargetPlanIsFree() {
    User user = User.builder().id(1L).build();
    CheckoutRequest request = new CheckoutRequest(PrecedentSearchPlan.FREE);

    assertThatThrownBy(() -> precedentSubscriptionService.checkout(user, request))
        .isInstanceOf(CustomException.class)
        .extracting(e -> ((CustomException) e).getErrorCode())
        .isEqualTo(PrecedentSubscriptionErrorCode.FREE_PLAN_NO_PAYMENT_REQUIRED);
  }

  @Test
  void checkout_throwsAlreadySubscribed_whenSamePlanAlreadyActive() {
    User user = User.builder().id(1L).build();
    PrecedentSubscription precedentSubscription =
        PrecedentSubscription.builder()
            .userId(1L)
            .plan(PrecedentSearchPlan.PREMIUM)
            .status(SubscriptionStatus.ACTIVE)
            .build();
    when(precedentSubscriptionRepository.findByUserId(1L))
        .thenReturn(Optional.of(precedentSubscription));
    CheckoutRequest request = new CheckoutRequest(PrecedentSearchPlan.PREMIUM);

    assertThatThrownBy(() -> precedentSubscriptionService.checkout(user, request))
        .isInstanceOf(CustomException.class)
        .extracting(e -> ((CustomException) e).getErrorCode())
        .isEqualTo(PrecedentSubscriptionErrorCode.ALREADY_SUBSCRIBED_PLAN);
  }

  @Test
  void checkout_savesPendingSessionAndReturnsGatewayInfo_whenValid() {
    User user = User.builder().id(1L).email("user@test.com").name("사용자").build();
    PrecedentSubscription precedentSubscription =
        PrecedentSubscription.builder()
            .userId(1L)
            .plan(PrecedentSearchPlan.FREE)
            .status(SubscriptionStatus.ACTIVE)
            .build();
    when(precedentSubscriptionRepository.findByUserId(1L))
        .thenReturn(Optional.of(precedentSubscription));
    when(paymentGateway.checkout(any()))
        .thenAnswer(
            invocation -> {
              PaymentCheckoutCommand command = invocation.getArgument(0);
              return new PaymentCheckoutInfo(
                  command.orderId(),
                  command.amount(),
                  command.orderName(),
                  command.customerEmail(),
                  command.customerName(),
                  "test_ck",
                  null);
            });

    CheckoutResponse result =
        precedentSubscriptionService.checkout(
            user, new CheckoutRequest(PrecedentSearchPlan.PREMIUM));

    assertThat(result.orderId()).startsWith("PSUB-");
    assertThat(result.clientKey()).isEqualTo("test_ck");
    verify(pendingCheckoutRepository)
        .save(
            eq(result.orderId()),
            eq(new PendingCheckout(1L, SubscriptionType.PRECEDENT_SEARCH, "PREMIUM", 14900)));
  }

  @Test
  void confirm_throwsInvalidSession_whenPendingCheckoutMissing() {
    User user = User.builder().id(1L).build();
    when(pendingCheckoutRepository.find("PSUB-abc")).thenReturn(Optional.empty());
    ConfirmRequest request = new ConfirmRequest("paymentKey", "PSUB-abc", 14900L);

    assertThatThrownBy(() -> precedentSubscriptionService.confirm(user, request))
        .isInstanceOf(CustomException.class)
        .extracting(e -> ((CustomException) e).getErrorCode())
        .isEqualTo(PrecedentSubscriptionErrorCode.INVALID_CHECKOUT_SESSION);
  }

  @Test
  void confirm_throwsInvalidSession_whenOwnerMismatch() {
    User user = User.builder().id(1L).build();
    when(pendingCheckoutRepository.find("PSUB-abc"))
        .thenReturn(
            Optional.of(
                new PendingCheckout(2L, SubscriptionType.PRECEDENT_SEARCH, "PREMIUM", 14900)));
    ConfirmRequest request = new ConfirmRequest("paymentKey", "PSUB-abc", 14900L);

    assertThatThrownBy(() -> precedentSubscriptionService.confirm(user, request))
        .isInstanceOf(CustomException.class)
        .extracting(e -> ((CustomException) e).getErrorCode())
        .isEqualTo(PrecedentSubscriptionErrorCode.INVALID_CHECKOUT_SESSION);
  }

  @Test
  void confirm_throwsInvalidSession_whenSubscriptionTypeMismatch() {
    User user = User.builder().id(1L).build();
    when(pendingCheckoutRepository.find("PSUB-abc"))
        .thenReturn(
            Optional.of(new PendingCheckout(1L, SubscriptionType.STORAGE, "STANDARD", 12900)));
    ConfirmRequest request = new ConfirmRequest("paymentKey", "PSUB-abc", 12900L);

    assertThatThrownBy(() -> precedentSubscriptionService.confirm(user, request))
        .isInstanceOf(CustomException.class)
        .extracting(e -> ((CustomException) e).getErrorCode())
        .isEqualTo(PrecedentSubscriptionErrorCode.INVALID_CHECKOUT_SESSION);
  }

  @Test
  void confirm_throwsAmountMismatch_whenAmountDiffersFromPendingSession() {
    User user = User.builder().id(1L).build();
    when(pendingCheckoutRepository.find("PSUB-abc"))
        .thenReturn(
            Optional.of(
                new PendingCheckout(1L, SubscriptionType.PRECEDENT_SEARCH, "PREMIUM", 14900)));
    ConfirmRequest request = new ConfirmRequest("paymentKey", "PSUB-abc", 99999L);

    assertThatThrownBy(() -> precedentSubscriptionService.confirm(user, request))
        .isInstanceOf(CustomException.class)
        .extracting(e -> ((CustomException) e).getErrorCode())
        .isEqualTo(PrecedentSubscriptionErrorCode.PAYMENT_AMOUNT_MISMATCH);
  }

  @Test
  void confirm_activatesSubscriptionAndSavesPayment_whenValid() {
    User user = User.builder().id(1L).build();
    PrecedentSubscription precedentSubscription =
        PrecedentSubscription.builder()
            .id(10L)
            .userId(1L)
            .plan(PrecedentSearchPlan.FREE)
            .status(SubscriptionStatus.ACTIVE)
            .build();
    when(pendingCheckoutRepository.find("PSUB-abc"))
        .thenReturn(
            Optional.of(
                new PendingCheckout(1L, SubscriptionType.PRECEDENT_SEARCH, "PREMIUM", 14900)));
    when(paymentGateway.confirm("paymentKey", "PSUB-abc", 14900))
        .thenReturn(
            new PaymentConfirmation("paymentKey", 14900, LocalDateTime.now(), "receipt-url"));
    when(paymentGateway.getProvider()).thenReturn(PaymentProvider.TOSS);
    when(precedentSubscriptionRepository.findByUserId(1L))
        .thenReturn(Optional.of(precedentSubscription));
    when(precedentSubscriptionMapper.toResponse(precedentSubscription))
        .thenReturn(
            PrecedentSubscriptionResponse.builder().plan(PrecedentSearchPlan.PREMIUM).build());
    ConfirmRequest request = new ConfirmRequest("paymentKey", "PSUB-abc", 14900L);

    PrecedentSubscriptionResponse result = precedentSubscriptionService.confirm(user, request);

    assertThat(result.plan()).isEqualTo(PrecedentSearchPlan.PREMIUM);
    assertThat(precedentSubscription.getPlan()).isEqualTo(PrecedentSearchPlan.PREMIUM);
    assertThat(precedentSubscription.getStatus()).isEqualTo(SubscriptionStatus.ACTIVE);

    ArgumentCaptor<Payment> paymentCaptor = ArgumentCaptor.forClass(Payment.class);
    verify(paymentRepository).save(paymentCaptor.capture());
    assertThat(paymentCaptor.getValue().getProviderPaymentKey()).isEqualTo("paymentKey");
    assertThat(paymentCaptor.getValue().getPlanCode()).isEqualTo("PREMIUM");
    assertThat(paymentCaptor.getValue().getSubscriptionType())
        .isEqualTo(SubscriptionType.PRECEDENT_SEARCH);
    verify(pendingCheckoutRepository).delete("PSUB-abc");
  }

  @Test
  void cancel_throwsNoActiveSubscription_whenPlanIsFree() {
    User user = User.builder().id(1L).build();
    PrecedentSubscription precedentSubscription =
        PrecedentSubscription.builder()
            .userId(1L)
            .plan(PrecedentSearchPlan.FREE)
            .status(SubscriptionStatus.ACTIVE)
            .build();
    when(precedentSubscriptionRepository.findByUserId(1L))
        .thenReturn(Optional.of(precedentSubscription));

    assertThatThrownBy(() -> precedentSubscriptionService.cancel(user))
        .isInstanceOf(CustomException.class)
        .extracting(e -> ((CustomException) e).getErrorCode())
        .isEqualTo(PrecedentSubscriptionErrorCode.NO_ACTIVE_PAID_SUBSCRIPTION);
  }

  @Test
  void cancel_cancelsSubscription_whenActivePaidPlan() {
    User user = User.builder().id(1L).build();
    PrecedentSubscription precedentSubscription =
        PrecedentSubscription.builder()
            .userId(1L)
            .plan(PrecedentSearchPlan.PREMIUM)
            .status(SubscriptionStatus.ACTIVE)
            .build();
    when(precedentSubscriptionRepository.findByUserId(1L))
        .thenReturn(Optional.of(precedentSubscription));
    when(precedentSubscriptionMapper.toResponse(precedentSubscription))
        .thenReturn(
            PrecedentSubscriptionResponse.builder().status(SubscriptionStatus.CANCELED).build());

    PrecedentSubscriptionResponse result = precedentSubscriptionService.cancel(user);

    assertThat(result.status()).isEqualTo(SubscriptionStatus.CANCELED);
    assertThat(precedentSubscription.getStatus()).isEqualTo(SubscriptionStatus.CANCELED);
    assertThat(precedentSubscription.getCanceledAt()).isNotNull();
  }
}
