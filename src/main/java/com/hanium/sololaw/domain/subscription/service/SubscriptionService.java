/* 
 * Copyright (c) HANIUM SOLOLAW 
 */
package com.hanium.sololaw.domain.subscription.service;

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
}
