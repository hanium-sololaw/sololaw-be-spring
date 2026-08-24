/* 
 * Copyright (c) HANIUM SOLOLAW 
 */
package com.hanium.sololaw.domain.payment.gateway;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import com.hanium.sololaw.domain.payment.entity.enums.PaymentProvider;
import com.hanium.sololaw.domain.payment.exception.PaymentErrorCode;
import com.hanium.sololaw.global.config.property.TossPaymentProperties;
import com.hanium.sololaw.global.exception.CustomException;

class TossPaymentGatewayTest {

  private MockRestServiceServer mockServer;
  private TossPaymentGateway gateway;

  @BeforeEach
  void setUp() {
    TossPaymentProperties properties =
        new TossPaymentProperties("test_ck", "test_sk", "https://api.tosspayments.com");
    RestClient.Builder builder = RestClient.builder();
    mockServer = MockRestServiceServer.bindTo(builder).build();
    gateway = new TossPaymentGateway(builder, properties);
  }

  @Test
  void checkout_returnsClientKeyAndOrderInfo_withoutCallingExternalApi() {
    PaymentCheckoutInfo info =
        gateway.checkout(
            new PaymentCheckoutCommand("SUB-1", 12900, "나홀로법에 STANDARD 플랜 구독", "a@b.com", "이름"));

    assertThat(info.orderId()).isEqualTo("SUB-1");
    assertThat(info.clientKey()).isEqualTo("test_ck");
    assertThat(info.redirectUrl()).isNull();
    mockServer.verify();
  }

  @Test
  void confirm_parsesReceiptUrlFromTossResponse() {
    mockServer
        .expect(requestTo("https://api.tosspayments.com/v1/payments/confirm"))
        .andExpect(method(HttpMethod.POST))
        .andRespond(
            withSuccess(
                "{\"receipt\":{\"url\":\"https://receipt.example.com\"}}",
                MediaType.APPLICATION_JSON));

    PaymentConfirmation result = gateway.confirm("paymentKey", "SUB-1", 12900);

    assertThat(result.providerPaymentKey()).isEqualTo("paymentKey");
    assertThat(result.receiptUrl()).isEqualTo("https://receipt.example.com");
    mockServer.verify();
  }

  @Test
  void confirm_throwsPaymentGatewayError_whenTossRespondsWithError() {
    mockServer
        .expect(requestTo("https://api.tosspayments.com/v1/payments/confirm"))
        .andRespond(withServerError());

    assertThatThrownBy(() -> gateway.confirm("paymentKey", "SUB-1", 12900))
        .isInstanceOf(CustomException.class)
        .extracting(e -> ((CustomException) e).getErrorCode())
        .isEqualTo(PaymentErrorCode.PAYMENT_GATEWAY_ERROR);
  }

  @Test
  void getProvider_returnsToss() {
    assertThat(gateway.getProvider()).isEqualTo(PaymentProvider.TOSS);
  }
}
