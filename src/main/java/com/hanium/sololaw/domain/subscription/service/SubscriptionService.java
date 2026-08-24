/* 
 * Copyright (c) HANIUM SOLOLAW 
 */
package com.hanium.sololaw.domain.subscription.service;

import com.hanium.sololaw.domain.subscription.dto.request.CheckoutRequest;
import com.hanium.sololaw.domain.subscription.dto.request.ConfirmRequest;
import com.hanium.sololaw.domain.subscription.dto.response.CheckoutResponse;
import com.hanium.sololaw.domain.subscription.dto.response.SubscriptionResponse;
import com.hanium.sololaw.domain.user.entity.User;

public interface SubscriptionService {

  /**
   * [ 내 구독 조회 메서드 ]
   *
   * @param user 로그인한 사용자(@CurrentUser로 주입)
   * @return 조회된 SubscriptionResponse. 구독 행은 회원가입 시점에 FREE 기본값으로 생성되어 항상 존재한다.
   */
  SubscriptionResponse getMySubscription(User user);

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
   * @return 갱신된 SubscriptionResponse
   */
  SubscriptionResponse confirm(User user, ConfirmRequest request);

  /**
   * [ 구독 해지 메서드 ]
   *
   * @param user 로그인한 사용자(@CurrentUser로 주입)
   * @return 갱신된 SubscriptionResponse
   */
  SubscriptionResponse cancel(User user);
}
