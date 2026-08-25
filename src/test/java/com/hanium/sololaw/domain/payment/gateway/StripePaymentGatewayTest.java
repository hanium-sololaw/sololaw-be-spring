/* 
 * Copyright (c) HANIUM SOLOLAW 
 */
package com.hanium.sololaw.domain.payment.gateway;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.hanium.sololaw.domain.payment.entity.enums.PaymentProvider;
import com.hanium.sololaw.domain.payment.exception.PaymentErrorCode;
import com.hanium.sololaw.global.config.property.StripeProperties;
import com.hanium.sololaw.global.exception.CustomException;
import com.stripe.StripeClient;
import com.stripe.exception.ApiConnectionException;
import com.stripe.model.checkout.Session;
import com.stripe.net.ApiRequest;
import com.stripe.net.StripeResponseGetter;

@ExtendWith(MockitoExtension.class)
class StripePaymentGatewayTest {

  @Mock private StripeResponseGetter responseGetter;

  private StripePaymentGateway gateway;

  @BeforeEach
  void setUp() {
    StripeProperties properties =
        new StripeProperties(
            "sk_test_dummy",
            "pk_test_dummy",
            "https://naholo-law-test.vercel.app/payment/success?session_id={CHECKOUT_SESSION_ID}",
            "https://naholo-law-test.vercel.app/payment/cancel",
            "whsec_dummy");
    StripeClient client = new StripeClient(responseGetter);
    gateway = new StripePaymentGateway(client, properties);
  }

  @Test
  void getProvider_returnsStripe() {
    assertThat(gateway.getProvider()).isEqualTo(PaymentProvider.STRIPE);
  }

  @Test
  void checkout_returnsRedirectUrl_fromCreatedSession() throws Exception {
    Session session = new Session();
    session.setUrl("https://checkout.stripe.com/c/pay/cs_test_abc");
    when(responseGetter.request(any(ApiRequest.class), any())).thenReturn(session);

    PaymentCheckoutInfo info =
        gateway.checkout(
            new PaymentCheckoutCommand("SUB-1", 12900, "나홀로법에 STANDARD 플랜 구독", "a@b.com", "이름"));

    assertThat(info.orderId()).isEqualTo("SUB-1");
    assertThat(info.clientKey()).isNull();
    assertThat(info.redirectUrl()).isEqualTo("https://checkout.stripe.com/c/pay/cs_test_abc");
  }

  @Test
  void checkout_throwsPaymentGatewayError_whenStripeCallFails() throws Exception {
    when(responseGetter.request(any(ApiRequest.class), any()))
        .thenThrow(new ApiConnectionException("network error"));

    assertThatThrownBy(
            () ->
                gateway.checkout(
                    new PaymentCheckoutCommand("SUB-1", 12900, "orderName", "a@b.com", "이름")))
        .isInstanceOf(CustomException.class)
        .extracting(e -> ((CustomException) e).getErrorCode())
        .isEqualTo(PaymentErrorCode.PAYMENT_GATEWAY_ERROR);
  }

  @Test
  void confirm_returnsPaymentIntentId_whenSessionIsPaidAndAmountMatches() throws Exception {
    Session session = new Session();
    session.setPaymentStatus("paid");
    session.setAmountTotal(12900L);
    session.setPaymentIntent("pi_test_123");
    when(responseGetter.request(any(ApiRequest.class), any())).thenReturn(session);

    PaymentConfirmation result = gateway.confirm("cs_test_abc", "SUB-1", 12900);

    assertThat(result.providerPaymentKey()).isEqualTo("pi_test_123");
    assertThat(result.amount()).isEqualTo(12900);
  }

  @Test
  void confirm_throwsPaymentGatewayError_whenSessionNotPaid() throws Exception {
    Session session = new Session();
    session.setPaymentStatus("unpaid");
    session.setAmountTotal(12900L);
    when(responseGetter.request(any(ApiRequest.class), any())).thenReturn(session);

    assertThatThrownBy(() -> gateway.confirm("cs_test_abc", "SUB-1", 12900))
        .isInstanceOf(CustomException.class)
        .extracting(e -> ((CustomException) e).getErrorCode())
        .isEqualTo(PaymentErrorCode.PAYMENT_GATEWAY_ERROR);
  }

  @Test
  void confirm_throwsPaymentGatewayError_whenAmountMismatch() throws Exception {
    Session session = new Session();
    session.setPaymentStatus("paid");
    session.setAmountTotal(99999L);
    when(responseGetter.request(any(ApiRequest.class), any())).thenReturn(session);

    assertThatThrownBy(() -> gateway.confirm("cs_test_abc", "SUB-1", 12900))
        .isInstanceOf(CustomException.class)
        .extracting(e -> ((CustomException) e).getErrorCode())
        .isEqualTo(PaymentErrorCode.PAYMENT_GATEWAY_ERROR);
  }

  @Test
  void cancel_throwsPaymentGatewayError_whenStripeCallFails() throws Exception {
    when(responseGetter.request(any(ApiRequest.class), any()))
        .thenThrow(new ApiConnectionException("network error"));

    assertThatThrownBy(() -> gateway.cancel("pi_test_123", "사용자 요청"))
        .isInstanceOf(CustomException.class)
        .extracting(e -> ((CustomException) e).getErrorCode())
        .isEqualTo(PaymentErrorCode.PAYMENT_GATEWAY_ERROR);
  }
}
