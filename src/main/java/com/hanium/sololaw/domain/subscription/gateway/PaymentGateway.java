/* 
 * Copyright (c) HANIUM SOLOLAW 
 */
package com.hanium.sololaw.domain.subscription.gateway;

import com.hanium.sololaw.domain.subscription.entity.enums.PaymentProvider;

/**
 * 결제대행사(PG) 연동 포트. 구현체를 교체하는 것만으로 다른 PG(Stripe 등)로 바꿔 끼울 수 있도록, 구독 도메인은 이 인터페이스에만 의존한다. 현재는 {@link
 * TossPaymentGateway} 하나만 존재하며, {@code payment.provider} 설정값으로 활성 구현체를 고른다.
 */
public interface PaymentGateway {

  /** 결제 기록에 남길 PG 식별자. 서비스 레이어가 provider를 하드코딩하지 않도록 활성 구현체가 직접 알려준다. */
  PaymentProvider getProvider();

  /**
   * 결제 시작 정보를 만든다. Toss처럼 프론트가 SDK로 직접 결제창을 띄우는 PG는 외부 API 호출 없이 위젯 초기화 정보만 돌려주고, Stripe Checkout처럼
   * 리다이렉트형 PG는 실제로 세션 생성 API를 호출해 redirectUrl을 채워 돌려준다.
   *
   * @param command 주문 정보(주문 ID, 금액, 주문명, 구매자 정보)
   * @return 프론트가 결제를 시작하는 데 필요한 정보
   */
  PaymentCheckoutInfo checkout(PaymentCheckoutCommand command);

  /**
   * 결제를 승인(검증)한다. 프론트에서 결제창 완료 후 전달받은 값을 그대로 PG 서버에 재확인해, 클라이언트가 위조한 금액으로 승인되는 것을 막는다.
   *
   * @param providerPaymentKey PG사 결제 키(Toss: paymentKey)
   * @param orderId 주문 ID
   * @param amount 결제 금액(원)
   * @return 검증된 결제 정보
   */
  PaymentConfirmation confirm(String providerPaymentKey, String orderId, long amount);

  /**
   * 결제를 취소(환불)한다.
   *
   * @param providerPaymentKey PG사 결제 키
   * @param reason 취소 사유
   */
  void cancel(String providerPaymentKey, String reason);
}
