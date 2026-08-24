/* 
 * Copyright (c) HANIUM SOLOLAW 
 */
package com.hanium.sololaw.domain.subscription.gateway;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Map;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import com.hanium.sololaw.domain.subscription.entity.enums.PaymentProvider;
import com.hanium.sololaw.domain.subscription.exception.SubscriptionErrorCode;
import com.hanium.sololaw.global.config.property.TossPaymentProperties;
import com.hanium.sololaw.global.exception.CustomException;

import lombok.extern.slf4j.Slf4j;
import tools.jackson.databind.JsonNode;

/** 토스페이먼츠 REST API 연동. {@code payment.provider=toss}(기본값)일 때 활성 {@link PaymentGateway} 구현체다. */
@Slf4j
@Component
@ConditionalOnProperty(
    prefix = "payment",
    name = "provider",
    havingValue = "toss",
    matchIfMissing = true)
public class TossPaymentGateway implements PaymentGateway {

  private final RestClient restClient;
  private final TossPaymentProperties properties;

  public TossPaymentGateway(
      RestClient.Builder restClientBuilder, TossPaymentProperties properties) {
    this.properties = properties;
    // 공유 빈일 수 있는 builder를 그대로 변경하면 다른 소비자에게 영향을 주므로 clone 후 설정한다.
    this.restClient = restClientBuilder.clone().baseUrl(properties.getBaseUrl()).build();
  }

  @Override
  public PaymentProvider getProvider() {
    return PaymentProvider.TOSS;
  }

  /** Toss는 결제창을 프론트가 SDK로 직접 띄우므로, 여기서는 외부 API 호출 없이 위젯 초기화에 필요한 clientKey와 주문 정보만 그대로 담아 돌려준다. */
  @Override
  public PaymentCheckoutInfo checkout(PaymentCheckoutCommand command) {
    return new PaymentCheckoutInfo(
        command.orderId(),
        command.amount(),
        command.orderName(),
        command.customerEmail(),
        command.customerName(),
        properties.getClientKey(),
        null);
  }

  @Override
  public PaymentConfirmation confirm(String paymentKey, String orderId, long amount) {
    log.debug("[TossPaymentGateway] confirm() - START | orderId: {}", orderId);

    Map<String, Object> body =
        Map.of("paymentKey", paymentKey, "orderId", orderId, "amount", amount);

    JsonNode response;
    try {
      response =
          restClient
              .post()
              .uri("/v1/payments/confirm")
              .headers(this::setAuthHeaders)
              .body(body)
              .retrieve()
              .body(JsonNode.class);
    } catch (RestClientException e) {
      log.error(
          "[TossPaymentGateway] confirm() - FAIL | orderId: {}, error: {}",
          orderId,
          e.getMessage());
      throw new CustomException(SubscriptionErrorCode.PAYMENT_GATEWAY_ERROR);
    }

    String receiptUrl =
        response != null && response.path("receipt").has("url")
            ? response.path("receipt").get("url").asText()
            : null;

    log.debug("[TossPaymentGateway] confirm() - END | orderId: {}", orderId);
    return new PaymentConfirmation(paymentKey, amount, LocalDateTime.now(), receiptUrl);
  }

  @Override
  public void cancel(String paymentKey, String reason) {
    log.debug("[TossPaymentGateway] cancel() - START | paymentKey: {}", paymentKey);

    try {
      restClient
          .post()
          .uri("/v1/payments/{paymentKey}/cancel", paymentKey)
          .headers(this::setAuthHeaders)
          .body(Map.of("cancelReason", reason))
          .retrieve()
          .toBodilessEntity();
    } catch (RestClientException e) {
      log.error(
          "[TossPaymentGateway] cancel() - FAIL | paymentKey: {}, error: {}",
          paymentKey,
          e.getMessage());
      throw new CustomException(SubscriptionErrorCode.PAYMENT_GATEWAY_ERROR);
    }

    log.debug("[TossPaymentGateway] cancel() - END | paymentKey: {}", paymentKey);
  }

  private void setAuthHeaders(HttpHeaders headers) {
    headers.setContentType(MediaType.APPLICATION_JSON);
    String encoded =
        Base64.getEncoder()
            .encodeToString((properties.getSecretKey() + ":").getBytes(StandardCharsets.UTF_8));
    headers.set(HttpHeaders.AUTHORIZATION, "Basic " + encoded);
  }
}
