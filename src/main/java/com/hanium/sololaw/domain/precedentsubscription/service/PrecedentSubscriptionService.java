/* 
 * Copyright (c) HANIUM SOLOLAW 
 */
package com.hanium.sololaw.domain.precedentsubscription.service;

import com.hanium.sololaw.domain.precedentsubscription.dto.request.CheckoutRequest;
import com.hanium.sololaw.domain.precedentsubscription.dto.request.ConfirmRequest;
import com.hanium.sololaw.domain.precedentsubscription.dto.response.CheckoutResponse;
import com.hanium.sololaw.domain.precedentsubscription.dto.response.PrecedentSubscriptionResponse;
import com.hanium.sololaw.domain.precedentsubscription.entity.enums.PrecedentSearchPlan;
import com.hanium.sololaw.domain.user.entity.User;

public interface PrecedentSubscriptionService {

  /**
   * [ 내 판례검색 구독 조회 메서드 ]
   *
   * @param user 로그인한 사용자(@CurrentUser로 주입)
   * @return 조회된 PrecedentSubscriptionResponse. 구독 행은 회원가입 시점에 FREE 기본값으로 생성되어 항상 존재한다.
   */
  PrecedentSubscriptionResponse getMyPrecedentSubscription(User user);

  /**
   * [ 결제 시작 메서드 ]
   *
   * @param user 로그인한 사용자(@CurrentUser로 주입)
   * @param request 구독할 플랜
   * @return 프론트가 결제창을 여는 데 필요한 주문 정보
   */
  CheckoutResponse checkout(User user, CheckoutRequest request);

  /**
   * [ 결제 승인 메서드 ]
   *
   * @param user 로그인한 사용자(@CurrentUser로 주입)
   * @param request 결제창 완료 후 전달받은 paymentKey·orderId·amount
   * @return 갱신된 PrecedentSubscriptionResponse
   */
  PrecedentSubscriptionResponse confirm(User user, ConfirmRequest request);

  /**
   * [ 구독 해지 메서드 ]
   *
   * @param user 로그인한 사용자(@CurrentUser로 주입)
   * @return 갱신된 PrecedentSubscriptionResponse
   */
  PrecedentSubscriptionResponse cancel(User user);

  /**
   * [ 현재 유효한 판례검색 플랜 실시간 계산 메서드 ]
   *
   * <p>저장된 plan·status는 해지 후 유예기간이 지나도 그대로 남아있을 수 있어(만료 처리 배치 없음) 그대로 신뢰할 수 없다. status와
   * nextBillingAt(다음 결제 예정일)을 함께 봐서 지금 시점에 실제로 유효한 플랜을 계산한다. 구독 행이 없거나 애매한 상태는 전부 FREE로 안전하게 처리하며
   * 예외를 던지지 않는다(nginx 내부 인증 검증 API에서 사용 — 실패해도 항상 폴백 가능해야 함).
   *
   * @param user 로그인한 사용자(@CurrentUser로 주입)
   * @return 지금 시점에 유효한 PrecedentSearchPlan
   */
  PrecedentSearchPlan getEffectivePlan(User user);
}
