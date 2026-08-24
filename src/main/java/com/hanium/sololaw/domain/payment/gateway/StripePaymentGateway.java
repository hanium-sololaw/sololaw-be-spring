/* 
 * Copyright (c) HANIUM SOLOLAW 
 */
package com.hanium.sololaw.domain.payment.gateway;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import com.hanium.sololaw.domain.payment.entity.enums.PaymentProvider;
import com.hanium.sololaw.domain.payment.exception.PaymentErrorCode;
import com.hanium.sololaw.global.config.property.StripeProperties;
import com.hanium.sololaw.global.exception.CustomException;
import com.stripe.StripeClient;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.param.RefundCreateParams;
import com.stripe.param.checkout.SessionCreateParams;

import lombok.extern.slf4j.Slf4j;

/**
 * Stripe Checkout 연동. {@code payment.provider=stripe}일 때 활성 {@link PaymentGateway} 구현체다. Toss와 달리
 * 결제창을 서버가 세션으로 만들어 리다이렉트시키는 방식이라 실제 Stripe API를 호출한다.
 *
 * <p>알려진 단순화: (1) receiptUrl은 채우지 않는다 — PaymentIntent에 연결된 Charge를 추가로 조회해야 하는데, 결제 검증 자체엔 필요하지 않아
 * 이번 구현에서는 생략했다. (2) cancel()의 환불 사유는 REQUESTED_BY_CUSTOMER로 고정하고, 호출부가 전달한 문자열은 Refund의 metadata에
 * 남긴다(Stripe reason은 정해진 enum만 허용한다).
 */
@Slf4j
@Component
@ConditionalOnProperty(prefix = "payment", name = "provider", havingValue = "stripe")
public class StripePaymentGateway implements PaymentGateway {

  private static final String KRW = "krw";

  private final StripeClient client;
  private final StripeProperties properties;

  @Autowired
  public StripePaymentGateway(StripeProperties properties) {
    this(new StripeClient(properties.getSecretKey()), properties);
  }

  /** StripeClient는 {@link com.stripe.net.StripeResponseGetter} 생성자로 테스트에서 실제 HTTP 없이 만들 수 있다. */
  StripePaymentGateway(StripeClient client, StripeProperties properties) {
    this.client = client;
    this.properties = properties;
  }

  @Override
  public PaymentProvider getProvider() {
    return PaymentProvider.STRIPE;
  }

  /**
   * Checkout Session을 생성하고 세션 URL을 redirectUrl로 돌려준다. successUrl에 orderId를 쿼리 파라미터로 덧붙여, 결제 완료 후
   * 프론트가 우리 confirm API에 필요한 orderId를 알 수 있게 한다(Stripe는 세션 ID만 돌려주고 우리가 만든 orderId는 모른다).
   */
  @Override
  public PaymentCheckoutInfo checkout(PaymentCheckoutCommand command) {
    log.debug("[StripePaymentGateway] checkout() - START | orderId: {}", command.orderId());

    SessionCreateParams params =
        SessionCreateParams.builder()
            .setMode(SessionCreateParams.Mode.PAYMENT)
            .setClientReferenceId(command.orderId())
            .setCustomerEmail(command.customerEmail())
            .setSuccessUrl(properties.getSuccessUrl() + "&order_id=" + command.orderId())
            .setCancelUrl(properties.getCancelUrl())
            .addLineItem(
                SessionCreateParams.LineItem.builder()
                    .setQuantity(1L)
                    .setPriceData(
                        SessionCreateParams.LineItem.PriceData.builder()
                            .setCurrency(KRW)
                            // KRW는 Stripe의 zero-decimal 통화라 unit_amount가 원 단위 그대로다(예: 12900원 =
                            // 12900).
                            .setUnitAmount(command.amount())
                            .setProductData(
                                SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                    .setName(command.orderName())
                                    .build())
                            .build())
                    .build())
            .build();

    Session session;
    try {
      session = client.v1().checkout().sessions().create(params);
    } catch (StripeException e) {
      log.error(
          "[StripePaymentGateway] checkout() - FAIL | orderId: {}, error: {}",
          command.orderId(),
          e.getMessage());
      throw new CustomException(PaymentErrorCode.PAYMENT_GATEWAY_ERROR);
    }

    log.debug("[StripePaymentGateway] checkout() - END | orderId: {}", command.orderId());
    return new PaymentCheckoutInfo(
        command.orderId(),
        command.amount(),
        command.orderName(),
        command.customerEmail(),
        command.customerName(),
        null,
        session.getUrl());
  }

  /**
   * providerPaymentKey로 Checkout Session ID를 받아 결제 상태·금액을 재검증한다. 검증된 결제 정보의 providerPaymentKey는
   * (환불에 바로 쓸 수 있도록) Session ID가 아니라 PaymentIntent ID로 바꿔 돌려준다.
   */
  @Override
  public PaymentConfirmation confirm(String checkoutSessionId, String orderId, long amount) {
    log.debug("[StripePaymentGateway] confirm() - START | orderId: {}", orderId);

    Session session;
    try {
      session = client.v1().checkout().sessions().retrieve(checkoutSessionId);
    } catch (StripeException e) {
      log.error(
          "[StripePaymentGateway] confirm() - FAIL | orderId: {}, error: {}",
          orderId,
          e.getMessage());
      throw new CustomException(PaymentErrorCode.PAYMENT_GATEWAY_ERROR);
    }

    if (!"paid".equals(session.getPaymentStatus()) || session.getAmountTotal() != amount) {
      log.error(
          "[StripePaymentGateway] confirm() - FAIL | orderId: {}, paymentStatus: {}, amountTotal: {}",
          orderId,
          session.getPaymentStatus(),
          session.getAmountTotal());
      throw new CustomException(PaymentErrorCode.PAYMENT_GATEWAY_ERROR);
    }

    log.debug("[StripePaymentGateway] confirm() - END | orderId: {}", orderId);
    return new PaymentConfirmation(session.getPaymentIntent(), amount, LocalDateTime.now(), null);
  }

  @Override
  public void cancel(String paymentIntentId, String reason) {
    log.debug("[StripePaymentGateway] cancel() - START | paymentIntentId: {}", paymentIntentId);

    RefundCreateParams params =
        RefundCreateParams.builder()
            .setPaymentIntent(paymentIntentId)
            .setReason(RefundCreateParams.Reason.REQUESTED_BY_CUSTOMER)
            .putMetadata("reason", reason)
            .build();

    try {
      client.v1().refunds().create(params);
    } catch (StripeException e) {
      log.error(
          "[StripePaymentGateway] cancel() - FAIL | paymentIntentId: {}, error: {}",
          paymentIntentId,
          e.getMessage());
      throw new CustomException(PaymentErrorCode.PAYMENT_GATEWAY_ERROR);
    }

    log.debug("[StripePaymentGateway] cancel() - END | paymentIntentId: {}", paymentIntentId);
  }
}
