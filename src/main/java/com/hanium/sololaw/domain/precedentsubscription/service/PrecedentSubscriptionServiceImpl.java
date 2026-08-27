/* 
 * Copyright (c) HANIUM SOLOLAW 
 */
package com.hanium.sololaw.domain.precedentsubscription.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hanium.sololaw.domain.payment.entity.Payment;
import com.hanium.sololaw.domain.payment.entity.enums.SubscriptionType;
import com.hanium.sololaw.domain.payment.gateway.PaymentCheckoutCommand;
import com.hanium.sololaw.domain.payment.gateway.PaymentCheckoutInfo;
import com.hanium.sololaw.domain.payment.gateway.PaymentConfirmation;
import com.hanium.sololaw.domain.payment.gateway.PaymentGateway;
import com.hanium.sololaw.domain.payment.repository.PaymentRepository;
import com.hanium.sololaw.domain.payment.repository.PendingCheckout;
import com.hanium.sololaw.domain.payment.repository.PendingCheckoutRepository;
import com.hanium.sololaw.domain.payment.service.PendingPaymentConfirmer;
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

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class PrecedentSubscriptionServiceImpl
    implements PrecedentSubscriptionService, PendingPaymentConfirmer {

  private final PrecedentSubscriptionRepository precedentSubscriptionRepository;
  private final PrecedentSubscriptionMapper precedentSubscriptionMapper;
  private final PaymentGateway paymentGateway;
  private final PaymentRepository paymentRepository;
  private final PendingCheckoutRepository pendingCheckoutRepository;

  @Override
  @Transactional(readOnly = true)
  public PrecedentSubscriptionResponse getMyPrecedentSubscription(User user) {
    log.info(
        "[PrecedentSubscriptionService] getMyPrecedentSubscription() - START | userId: {}",
        user.getId());

    /*
       (1) 판례검색 구독 조회
       - 회원가입 시점에 AuthServiceImpl이 FREE 기본값 행을 생성하므로 항상 존재해야 한다.
    */
    PrecedentSubscription precedentSubscription = findPrecedentSubscription(user.getId());

    /*
       (2) ResponseDto Mapping
    */
    PrecedentSubscriptionResponse result =
        precedentSubscriptionMapper.toResponse(precedentSubscription);

    log.info(
        "[PrecedentSubscriptionService] getMyPrecedentSubscription() - END | userId: {}",
        user.getId());
    return result;
  }

  @Override
  @Transactional(readOnly = true)
  public CheckoutResponse checkout(User user, CheckoutRequest request) {
    log.info(
        "[PrecedentSubscriptionService] checkout() - START | userId: {}, plan: {}",
        user.getId(),
        request.plan());

    /*
       (1) 플랜 검증
       - FREE는 결제 대상이 아니고, 이미 구독 중인 플랜을 다시 결제할 수 없다.
    */
    PrecedentSearchPlan targetPlan = request.plan();
    if (targetPlan == PrecedentSearchPlan.FREE) {
      throw new CustomException(PrecedentSubscriptionErrorCode.FREE_PLAN_NO_PAYMENT_REQUIRED);
    }
    PrecedentSubscription precedentSubscription = findPrecedentSubscription(user.getId());
    if (precedentSubscription.getStatus() == SubscriptionStatus.ACTIVE
        && precedentSubscription.getPlan() == targetPlan) {
      throw new CustomException(PrecedentSubscriptionErrorCode.ALREADY_SUBSCRIBED_PLAN);
    }

    /*
       (2) 주문 ID 생성 및 결제 대기 세션 저장
       - confirm 시점에 클라이언트가 위조한 플랜·금액이 아니라 여기서 저장한 서버 기준값으로 검증한다.
    */
    String orderId = "PSUB-" + UUID.randomUUID();
    pendingCheckoutRepository.save(
        orderId,
        new PendingCheckout(
            user.getId(),
            SubscriptionType.PRECEDENT_SEARCH,
            targetPlan.name(),
            targetPlan.getPriceKrw()));

    /*
       (3) 결제 게이트웨이에 체크아웃 정보 요청
    */
    String orderName = "나홀로법에 " + targetPlan.name() + " 플랜 구독";
    PaymentCheckoutInfo info =
        paymentGateway.checkout(
            new PaymentCheckoutCommand(
                orderId, targetPlan.getPriceKrw(), orderName, user.getEmail(), user.getName()));

    CheckoutResponse result =
        CheckoutResponse.builder()
            .orderId(info.orderId())
            .amount(info.amount())
            .orderName(info.orderName())
            .customerEmail(info.customerEmail())
            .customerName(info.customerName())
            .clientKey(info.clientKey())
            .redirectUrl(info.redirectUrl())
            .build();

    log.info(
        "[PrecedentSubscriptionService] checkout() - END | userId: {}, orderId: {}",
        user.getId(),
        orderId);
    return result;
  }

  @Override
  @Transactional
  public PrecedentSubscriptionResponse confirm(User user, ConfirmRequest request) {
    log.info(
        "[PrecedentSubscriptionService] confirm() - START | userId: {}, orderId: {}",
        user.getId(),
        request.orderId());

    /*
       (1) 결제 대기 세션 검증
       - orderId가 없거나(만료·오타), 다른 사용자의 세션이거나, 저장공간 구독 체크아웃 세션이면 거부한다.
    */
    PendingCheckout pending =
        pendingCheckoutRepository
            .find(request.orderId())
            .orElseThrow(
                () -> new CustomException(PrecedentSubscriptionErrorCode.INVALID_CHECKOUT_SESSION));
    if (!pending.userId().equals(user.getId())
        || pending.subscriptionType() != SubscriptionType.PRECEDENT_SEARCH) {
      throw new CustomException(PrecedentSubscriptionErrorCode.INVALID_CHECKOUT_SESSION);
    }
    if (pending.amount() != request.amount()) {
      throw new CustomException(PrecedentSubscriptionErrorCode.PAYMENT_AMOUNT_MISMATCH);
    }

    /*
       (2) 결제 게이트웨이 승인 API 호출
       - 클라이언트가 보낸 값이 아니라 PG 서버가 실제로 승인한 결제만 신뢰한다.
    */
    PaymentConfirmation confirmation =
        paymentGateway.confirm(request.paymentKey(), request.orderId(), request.amount());

    /*
       (3) 구독 활성화 — Stripe 웹훅과 공유하는 로직(confirmPending)에 위임한다.
    */
    confirmPending(pending, request.orderId(), confirmation);

    PrecedentSubscriptionResponse result =
        precedentSubscriptionMapper.toResponse(findPrecedentSubscription(user.getId()));

    log.info(
        "[PrecedentSubscriptionService] confirm() - END | userId: {}, plan: {}",
        user.getId(),
        pending.planCode());
    return result;
  }

  @Override
  public SubscriptionType getSubscriptionType() {
    return SubscriptionType.PRECEDENT_SEARCH;
  }

  @Override
  @Transactional
  public void confirmPending(
      PendingCheckout pending, String orderId, PaymentConfirmation confirmation) {
    /*
       (1) 구독 활성화
    */
    PrecedentSubscription precedentSubscription = findPrecedentSubscription(pending.userId());
    PrecedentSearchPlan targetPlan = PrecedentSearchPlan.valueOf(pending.planCode());
    LocalDateTime nextBillingAt = LocalDateTime.now().plusMonths(1);
    precedentSubscription.activatePrecedentPlan(targetPlan, nextBillingAt);
    precedentSubscriptionRepository.save(precedentSubscription);

    /*
       (2) 결제 이력 저장
    */
    paymentRepository.save(
        Payment.builder()
            .userId(pending.userId())
            .subscriptionId(precedentSubscription.getId())
            .subscriptionType(SubscriptionType.PRECEDENT_SEARCH)
            .planCode(pending.planCode())
            .amount(BigDecimal.valueOf(pending.amount()))
            .provider(paymentGateway.getProvider())
            .providerPaymentKey(confirmation.providerPaymentKey())
            .providerOrderId(orderId)
            .paidAt(confirmation.paidAt())
            .receiptUrl(confirmation.receiptUrl())
            .build());

    /*
       (3) 결제 대기 세션 제거
    */
    pendingCheckoutRepository.delete(orderId);
  }

  @Override
  @Transactional
  public PrecedentSubscriptionResponse cancel(User user) {
    log.info("[PrecedentSubscriptionService] cancel() - START | userId: {}", user.getId());

    /*
       (1) 해지 가능 여부 확인
       - FREE 플랜이거나 이미 비활성 상태면 해지할 대상이 없다.
    */
    PrecedentSubscription precedentSubscription = findPrecedentSubscription(user.getId());
    if (precedentSubscription.getPlan() == PrecedentSearchPlan.FREE
        || precedentSubscription.getStatus() != SubscriptionStatus.ACTIVE) {
      throw new CustomException(PrecedentSubscriptionErrorCode.NO_ACTIVE_PAID_SUBSCRIPTION);
    }

    /*
       (2) 구독 해지
       - 다음 결제 주기부터 갱신되지 않으며, 현재 플랜은 만료 전까지 유지된다.
    */
    precedentSubscription.cancel();
    precedentSubscriptionRepository.save(precedentSubscription);

    PrecedentSubscriptionResponse result =
        precedentSubscriptionMapper.toResponse(precedentSubscription);

    log.info("[PrecedentSubscriptionService] cancel() - END | userId: {}", user.getId());
    return result;
  }

  @Override
  @Transactional(readOnly = true)
  public PrecedentSearchPlan getEffectivePlan(User user) {
    return precedentSubscriptionRepository
        .findByUserId(user.getId())
        .map(this::resolveEffectivePlan)
        .orElse(PrecedentSearchPlan.FREE);
  }

  private PrecedentSearchPlan resolveEffectivePlan(PrecedentSubscription precedentSubscription) {
    if (precedentSubscription.getStatus() == SubscriptionStatus.ACTIVE) {
      return precedentSubscription.getPlan();
    }
    LocalDateTime nextBillingAt = precedentSubscription.getNextBillingAt();
    if (precedentSubscription.getStatus() == SubscriptionStatus.CANCELED
        && nextBillingAt != null
        && nextBillingAt.isAfter(LocalDateTime.now())) {
      return precedentSubscription.getPlan();
    }
    return PrecedentSearchPlan.FREE;
  }

  private PrecedentSubscription findPrecedentSubscription(Long userId) {
    return precedentSubscriptionRepository
        .findByUserId(userId)
        .orElseThrow(
            () ->
                new CustomException(
                    PrecedentSubscriptionErrorCode.PRECEDENT_SUBSCRIPTION_NOT_FOUND));
  }
}
