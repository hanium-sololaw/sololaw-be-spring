/* 
 * Copyright (c) HANIUM SOLOLAW 
 */
package com.hanium.sololaw.domain.subscription.service;

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
import com.hanium.sololaw.domain.subscription.dto.request.CheckoutRequest;
import com.hanium.sololaw.domain.subscription.dto.request.ConfirmRequest;
import com.hanium.sololaw.domain.subscription.dto.response.CheckoutResponse;
import com.hanium.sololaw.domain.subscription.dto.response.SubscriptionResponse;
import com.hanium.sololaw.domain.subscription.entity.Subscription;
import com.hanium.sololaw.domain.subscription.entity.enums.StoragePlan;
import com.hanium.sololaw.domain.subscription.entity.enums.SubscriptionStatus;
import com.hanium.sololaw.domain.subscription.exception.SubscriptionErrorCode;
import com.hanium.sololaw.domain.subscription.mapper.SubscriptionMapper;
import com.hanium.sololaw.domain.subscription.repository.SubscriptionRepository;
import com.hanium.sololaw.domain.user.entity.User;
import com.hanium.sololaw.global.exception.CustomException;

@ExtendWith(MockitoExtension.class)
class SubscriptionServiceTest {

  @Mock private SubscriptionRepository subscriptionRepository;
  @Mock private SubscriptionMapper subscriptionMapper;
  @Mock private PaymentGateway paymentGateway;
  @Mock private PaymentRepository paymentRepository;
  @Mock private PendingCheckoutRepository pendingCheckoutRepository;

  @InjectMocks private SubscriptionServiceImpl subscriptionService;

  @Test
  void getMySubscription_returnsMappedResponse_whenFound() {
    User user = User.builder().id(1L).build();
    Subscription subscription =
        Subscription.builder()
            .id(10L)
            .userId(1L)
            .plan(StoragePlan.FREE)
            .status(SubscriptionStatus.ACTIVE)
            .build();
    SubscriptionResponse expected =
        SubscriptionResponse.builder()
            .plan(StoragePlan.FREE)
            .status(SubscriptionStatus.ACTIVE)
            .build();
    when(subscriptionRepository.findByUserId(1L)).thenReturn(Optional.of(subscription));
    when(subscriptionMapper.toResponse(subscription)).thenReturn(expected);

    SubscriptionResponse result = subscriptionService.getMySubscription(user);

    assertThat(result).isEqualTo(expected);
  }

  @Test
  void getMySubscription_throwsNotFound_whenMissing() {
    User user = User.builder().id(1L).build();
    when(subscriptionRepository.findByUserId(1L)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> subscriptionService.getMySubscription(user))
        .isInstanceOf(CustomException.class)
        .extracting(e -> ((CustomException) e).getErrorCode())
        .isEqualTo(SubscriptionErrorCode.SUBSCRIPTION_NOT_FOUND);
  }

  @Test
  void checkout_throwsFreePlanError_whenTargetPlanIsFree() {
    User user = User.builder().id(1L).build();
    CheckoutRequest request = new CheckoutRequest(StoragePlan.FREE);

    assertThatThrownBy(() -> subscriptionService.checkout(user, request))
        .isInstanceOf(CustomException.class)
        .extracting(e -> ((CustomException) e).getErrorCode())
        .isEqualTo(SubscriptionErrorCode.FREE_PLAN_NO_PAYMENT_REQUIRED);
  }

  @Test
  void checkout_throwsAlreadySubscribed_whenSamePlanAlreadyActive() {
    User user = User.builder().id(1L).build();
    Subscription subscription =
        Subscription.builder()
            .userId(1L)
            .plan(StoragePlan.STANDARD)
            .status(SubscriptionStatus.ACTIVE)
            .build();
    when(subscriptionRepository.findByUserId(1L)).thenReturn(Optional.of(subscription));
    CheckoutRequest request = new CheckoutRequest(StoragePlan.STANDARD);

    assertThatThrownBy(() -> subscriptionService.checkout(user, request))
        .isInstanceOf(CustomException.class)
        .extracting(e -> ((CustomException) e).getErrorCode())
        .isEqualTo(SubscriptionErrorCode.ALREADY_SUBSCRIBED_PLAN);
  }

  @Test
  void checkout_savesPendingSessionAndReturnsGatewayInfo_whenValid() {
    User user = User.builder().id(1L).email("user@test.com").name("사용자").build();
    Subscription subscription =
        Subscription.builder()
            .userId(1L)
            .plan(StoragePlan.FREE)
            .status(SubscriptionStatus.ACTIVE)
            .build();
    when(subscriptionRepository.findByUserId(1L)).thenReturn(Optional.of(subscription));
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
        subscriptionService.checkout(user, new CheckoutRequest(StoragePlan.STANDARD));

    assertThat(result.orderId()).startsWith("SUB-");
    assertThat(result.clientKey()).isEqualTo("test_ck");
    verify(pendingCheckoutRepository)
        .save(
            eq(result.orderId()),
            eq(new PendingCheckout(1L, SubscriptionType.STORAGE, "STANDARD", 12900)));
  }

  @Test
  void confirm_throwsInvalidSession_whenPendingCheckoutMissing() {
    User user = User.builder().id(1L).build();
    when(pendingCheckoutRepository.find("SUB-abc")).thenReturn(Optional.empty());
    ConfirmRequest request = new ConfirmRequest("paymentKey", "SUB-abc", 12900L);

    assertThatThrownBy(() -> subscriptionService.confirm(user, request))
        .isInstanceOf(CustomException.class)
        .extracting(e -> ((CustomException) e).getErrorCode())
        .isEqualTo(SubscriptionErrorCode.INVALID_CHECKOUT_SESSION);
  }

  @Test
  void confirm_throwsInvalidSession_whenOwnerMismatch() {
    User user = User.builder().id(1L).build();
    when(pendingCheckoutRepository.find("SUB-abc"))
        .thenReturn(
            Optional.of(new PendingCheckout(2L, SubscriptionType.STORAGE, "STANDARD", 12900)));
    ConfirmRequest request = new ConfirmRequest("paymentKey", "SUB-abc", 12900L);

    assertThatThrownBy(() -> subscriptionService.confirm(user, request))
        .isInstanceOf(CustomException.class)
        .extracting(e -> ((CustomException) e).getErrorCode())
        .isEqualTo(SubscriptionErrorCode.INVALID_CHECKOUT_SESSION);
  }

  @Test
  void confirm_throwsInvalidSession_whenSubscriptionTypeMismatch() {
    User user = User.builder().id(1L).build();
    when(pendingCheckoutRepository.find("SUB-abc"))
        .thenReturn(
            Optional.of(
                new PendingCheckout(1L, SubscriptionType.PRECEDENT_SEARCH, "PREMIUM", 12900)));
    ConfirmRequest request = new ConfirmRequest("paymentKey", "SUB-abc", 12900L);

    assertThatThrownBy(() -> subscriptionService.confirm(user, request))
        .isInstanceOf(CustomException.class)
        .extracting(e -> ((CustomException) e).getErrorCode())
        .isEqualTo(SubscriptionErrorCode.INVALID_CHECKOUT_SESSION);
  }

  @Test
  void confirm_throwsAmountMismatch_whenAmountDiffersFromPendingSession() {
    User user = User.builder().id(1L).build();
    when(pendingCheckoutRepository.find("SUB-abc"))
        .thenReturn(
            Optional.of(new PendingCheckout(1L, SubscriptionType.STORAGE, "STANDARD", 12900)));
    ConfirmRequest request = new ConfirmRequest("paymentKey", "SUB-abc", 99999L);

    assertThatThrownBy(() -> subscriptionService.confirm(user, request))
        .isInstanceOf(CustomException.class)
        .extracting(e -> ((CustomException) e).getErrorCode())
        .isEqualTo(SubscriptionErrorCode.PAYMENT_AMOUNT_MISMATCH);
  }

  @Test
  void confirm_activatesSubscriptionAndSavesPayment_whenValid() {
    User user = User.builder().id(1L).build();
    Subscription subscription =
        Subscription.builder()
            .id(10L)
            .userId(1L)
            .plan(StoragePlan.FREE)
            .status(SubscriptionStatus.ACTIVE)
            .build();
    when(pendingCheckoutRepository.find("SUB-abc"))
        .thenReturn(
            Optional.of(new PendingCheckout(1L, SubscriptionType.STORAGE, "STANDARD", 12900)));
    when(paymentGateway.confirm("paymentKey", "SUB-abc", 12900))
        .thenReturn(
            new PaymentConfirmation("paymentKey", 12900, LocalDateTime.now(), "receipt-url"));
    when(paymentGateway.getProvider()).thenReturn(PaymentProvider.TOSS);
    when(subscriptionRepository.findByUserId(1L)).thenReturn(Optional.of(subscription));
    when(subscriptionMapper.toResponse(subscription))
        .thenReturn(SubscriptionResponse.builder().plan(StoragePlan.STANDARD).build());
    ConfirmRequest request = new ConfirmRequest("paymentKey", "SUB-abc", 12900L);

    SubscriptionResponse result = subscriptionService.confirm(user, request);

    assertThat(result.plan()).isEqualTo(StoragePlan.STANDARD);
    assertThat(subscription.getPlan()).isEqualTo(StoragePlan.STANDARD);
    assertThat(subscription.getStatus()).isEqualTo(SubscriptionStatus.ACTIVE);

    ArgumentCaptor<Payment> paymentCaptor = ArgumentCaptor.forClass(Payment.class);
    verify(paymentRepository).save(paymentCaptor.capture());
    assertThat(paymentCaptor.getValue().getProviderPaymentKey()).isEqualTo("paymentKey");
    assertThat(paymentCaptor.getValue().getPlanCode()).isEqualTo("STANDARD");
    assertThat(paymentCaptor.getValue().getSubscriptionType()).isEqualTo(SubscriptionType.STORAGE);
    verify(pendingCheckoutRepository).delete("SUB-abc");
  }

  @Test
  void cancel_throwsNoActiveSubscription_whenPlanIsFree() {
    User user = User.builder().id(1L).build();
    Subscription subscription =
        Subscription.builder()
            .userId(1L)
            .plan(StoragePlan.FREE)
            .status(SubscriptionStatus.ACTIVE)
            .build();
    when(subscriptionRepository.findByUserId(1L)).thenReturn(Optional.of(subscription));

    assertThatThrownBy(() -> subscriptionService.cancel(user))
        .isInstanceOf(CustomException.class)
        .extracting(e -> ((CustomException) e).getErrorCode())
        .isEqualTo(SubscriptionErrorCode.NO_ACTIVE_PAID_SUBSCRIPTION);
  }

  @Test
  void cancel_cancelsSubscription_whenActivePaidPlan() {
    User user = User.builder().id(1L).build();
    Subscription subscription =
        Subscription.builder()
            .userId(1L)
            .plan(StoragePlan.STANDARD)
            .status(SubscriptionStatus.ACTIVE)
            .build();
    when(subscriptionRepository.findByUserId(1L)).thenReturn(Optional.of(subscription));
    when(subscriptionMapper.toResponse(subscription))
        .thenReturn(SubscriptionResponse.builder().status(SubscriptionStatus.CANCELED).build());

    SubscriptionResponse result = subscriptionService.cancel(user);

    assertThat(result.status()).isEqualTo(SubscriptionStatus.CANCELED);
    assertThat(subscription.getStatus()).isEqualTo(SubscriptionStatus.CANCELED);
    assertThat(subscription.getCanceledAt()).isNotNull();
  }
}
