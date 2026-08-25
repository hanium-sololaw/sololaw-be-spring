/* 
 * Copyright (c) HANIUM SOLOLAW 
 */
package com.hanium.sololaw.domain.payment.service;

import com.hanium.sololaw.domain.payment.entity.enums.SubscriptionType;
import com.hanium.sololaw.domain.payment.gateway.PaymentConfirmation;
import com.hanium.sololaw.domain.payment.repository.PendingCheckout;

/**
 * 결제 대기 세션을 실제 구독 활성화로 이어주는 포트. 컨트롤러 경로(confirm API)와 Stripe 웹훅 경로가 같은 활성화 로직을 타도록, 저장공간·판례검색 구독
 * 서비스가 각각 이 인터페이스를 구현한다. PG 재검증({@code paymentGateway.confirm()})은 호출부(컨트롤러 또는 웹훅 서비스)에서 이미 끝낸 뒤
 * 결과만 넘겨준다 — 이 메서드는 검증된 결제를 구독 활성화로 반영하기만 한다. 웹훅은 인증된 사용자 세션이 없으므로, pending에 담긴 userId를 그대로
 * 신뢰한다(pending 자체가 checkout() 시점에 서버가 만든 값이라 위조 불가능하다).
 */
public interface PendingPaymentConfirmer {

  SubscriptionType getSubscriptionType();

  /**
   * 검증된 결제를 반영해 구독을 활성화하고 결제 이력을 남긴다. 대기 세션 삭제까지 이 메서드가 책임진다.
   *
   * @param pending 결제 대기 세션(체크아웃 시점에 저장된 서버 기준값)
   * @param orderId 우리 내부 주문 ID
   * @param confirmation PG 재검증을 마친 결제 확인 정보
   */
  void confirmPending(PendingCheckout pending, String orderId, PaymentConfirmation confirmation);
}
