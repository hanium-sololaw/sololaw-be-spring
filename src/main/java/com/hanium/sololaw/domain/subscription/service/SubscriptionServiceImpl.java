/* 
 * Copyright (c) HANIUM SOLOLAW 
 */
package com.hanium.sololaw.domain.subscription.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hanium.sololaw.domain.subscription.dto.request.CheckoutRequest;
import com.hanium.sololaw.domain.subscription.dto.request.ConfirmRequest;
import com.hanium.sololaw.domain.subscription.dto.response.CheckoutResponse;
import com.hanium.sololaw.domain.subscription.dto.response.SubscriptionResponse;
import com.hanium.sololaw.domain.subscription.entity.Payment;
import com.hanium.sololaw.domain.subscription.entity.Subscription;
import com.hanium.sololaw.domain.subscription.entity.enums.StoragePlan;
import com.hanium.sololaw.domain.subscription.entity.enums.SubscriptionStatus;
import com.hanium.sololaw.domain.subscription.exception.SubscriptionErrorCode;
import com.hanium.sololaw.domain.subscription.gateway.PaymentCheckoutCommand;
import com.hanium.sololaw.domain.subscription.gateway.PaymentCheckoutInfo;
import com.hanium.sololaw.domain.subscription.gateway.PaymentConfirmation;
import com.hanium.sololaw.domain.subscription.gateway.PaymentGateway;
import com.hanium.sololaw.domain.subscription.mapper.SubscriptionMapper;
import com.hanium.sololaw.domain.subscription.repository.PaymentRepository;
import com.hanium.sololaw.domain.subscription.repository.PendingCheckout;
import com.hanium.sololaw.domain.subscription.repository.PendingCheckoutRepository;
import com.hanium.sololaw.domain.subscription.repository.SubscriptionRepository;
import com.hanium.sololaw.domain.user.entity.User;
import com.hanium.sololaw.global.exception.CustomException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class SubscriptionServiceImpl implements SubscriptionService {

  private final SubscriptionRepository subscriptionRepository;
  private final SubscriptionMapper subscriptionMapper;
  private final PaymentGateway paymentGateway;
  private final PaymentRepository paymentRepository;
  private final PendingCheckoutRepository pendingCheckoutRepository;

  @Override
  @Transactional(readOnly = true)
  public SubscriptionResponse getMySubscription(User user) {
    log.info("[SubscriptionService] getMySubscription() - START | userId: {}", user.getId());

    /*
       (1) 구독 조회
       - 회원가입 시점에 AuthServiceImpl이 FREE 기본값 행을 생성하므로 항상 존재해야 한다.
    */
    Subscription subscription = findSubscription(user.getId());

    /*
       (2) ResponseDto Mapping
    */
    SubscriptionResponse result = subscriptionMapper.toResponse(subscription);

    log.info("[SubscriptionService] getMySubscription() - END | userId: {}", user.getId());
    return result;
  }

  @Override
  @Transactional(readOnly = true)
  public CheckoutResponse checkout(User user, CheckoutRequest request) {
    log.info(
        "[SubscriptionService] checkout() - START | userId: {}, plan: {}",
        user.getId(),
        request.plan());

    /*
       (1) 플랜 검증
       - FREE는 결제 대상이 아니고, 이미 구독 중인 플랜을 다시 결제할 수 없다.
    */
    StoragePlan targetPlan = request.plan();
    if (targetPlan == StoragePlan.FREE) {
      throw new CustomException(SubscriptionErrorCode.FREE_PLAN_NO_PAYMENT_REQUIRED);
    }
    Subscription subscription = findSubscription(user.getId());
    if (subscription.getStatus() == SubscriptionStatus.ACTIVE
        && subscription.getPlan() == targetPlan) {
      throw new CustomException(SubscriptionErrorCode.ALREADY_SUBSCRIBED_PLAN);
    }

    /*
       (2) 주문 ID 생성 및 결제 대기 세션 저장
       - confirm 시점에 클라이언트가 위조한 플랜·금액이 아니라 여기서 저장한 서버 기준값으로 검증한다.
    */
    String orderId = "SUB-" + UUID.randomUUID();
    pendingCheckoutRepository.save(
        orderId, new PendingCheckout(user.getId(), targetPlan, targetPlan.getPriceKrw()));

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
        "[SubscriptionService] checkout() - END | userId: {}, orderId: {}", user.getId(), orderId);
    return result;
  }

  @Override
  @Transactional
  public SubscriptionResponse confirm(User user, ConfirmRequest request) {
    log.info(
        "[SubscriptionService] confirm() - START | userId: {}, orderId: {}",
        user.getId(),
        request.orderId());

    /*
       (1) 결제 대기 세션 검증
       - orderId가 없거나(만료·오타) 다른 사용자의 세션이면 거부한다.
    */
    PendingCheckout pending =
        pendingCheckoutRepository
            .find(request.orderId())
            .orElseThrow(() -> new CustomException(SubscriptionErrorCode.INVALID_CHECKOUT_SESSION));
    if (!pending.userId().equals(user.getId())) {
      throw new CustomException(SubscriptionErrorCode.INVALID_CHECKOUT_SESSION);
    }
    if (pending.amount() != request.amount()) {
      throw new CustomException(SubscriptionErrorCode.PAYMENT_AMOUNT_MISMATCH);
    }

    /*
       (2) 결제 게이트웨이 승인 API 호출
       - 클라이언트가 보낸 값이 아니라 PG 서버가 실제로 승인한 결제만 신뢰한다.
    */
    PaymentConfirmation confirmation =
        paymentGateway.confirm(request.paymentKey(), request.orderId(), request.amount());

    /*
       (3) 구독 활성화
    */
    Subscription subscription = findSubscription(user.getId());
    LocalDateTime nextBillingAt = LocalDateTime.now().plusMonths(1);
    subscription.activatePaidPlan(pending.plan(), nextBillingAt);
    subscriptionRepository.save(subscription);

    /*
       (4) 결제 이력 저장
    */
    paymentRepository.save(
        Payment.builder()
            .userId(user.getId())
            .subscriptionId(subscription.getId())
            .plan(pending.plan())
            .amount(BigDecimal.valueOf(pending.amount()))
            .provider(paymentGateway.getProvider())
            .providerPaymentKey(confirmation.providerPaymentKey())
            .providerOrderId(request.orderId())
            .paidAt(confirmation.paidAt())
            .receiptUrl(confirmation.receiptUrl())
            .build());

    /*
       (5) 결제 대기 세션 제거
    */
    pendingCheckoutRepository.delete(request.orderId());

    SubscriptionResponse result = subscriptionMapper.toResponse(subscription);

    log.info(
        "[SubscriptionService] confirm() - END | userId: {}, plan: {}",
        user.getId(),
        pending.plan());
    return result;
  }

  @Override
  @Transactional
  public SubscriptionResponse cancel(User user) {
    log.info("[SubscriptionService] cancel() - START | userId: {}", user.getId());

    /*
       (1) 해지 가능 여부 확인
       - FREE 플랜이거나 이미 비활성 상태면 해지할 대상이 없다.
    */
    Subscription subscription = findSubscription(user.getId());
    if (subscription.getPlan() == StoragePlan.FREE
        || subscription.getStatus() != SubscriptionStatus.ACTIVE) {
      throw new CustomException(SubscriptionErrorCode.NO_ACTIVE_PAID_SUBSCRIPTION);
    }

    /*
       (2) 구독 해지
       - 다음 결제 주기부터 갱신되지 않으며, 현재 플랜은 만료 전까지 유지된다.
    */
    subscription.cancel();
    subscriptionRepository.save(subscription);

    SubscriptionResponse result = subscriptionMapper.toResponse(subscription);

    log.info("[SubscriptionService] cancel() - END | userId: {}", user.getId());
    return result;
  }

  private Subscription findSubscription(Long userId) {
    return subscriptionRepository
        .findByUserId(userId)
        .orElseThrow(() -> new CustomException(SubscriptionErrorCode.SUBSCRIPTION_NOT_FOUND));
  }
}
