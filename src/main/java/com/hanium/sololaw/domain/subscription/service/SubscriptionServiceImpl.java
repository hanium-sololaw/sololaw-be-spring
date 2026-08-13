/* 
 * Copyright (c) HANIUM SOLOLAW 
 */
package com.hanium.sololaw.domain.subscription.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hanium.sololaw.domain.subscription.dto.response.SubscriptionResponse;
import com.hanium.sololaw.domain.subscription.entity.Subscription;
import com.hanium.sololaw.domain.subscription.exception.SubscriptionErrorCode;
import com.hanium.sololaw.domain.subscription.mapper.SubscriptionMapper;
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

  @Override
  @Transactional(readOnly = true)
  public SubscriptionResponse getMySubscription(User user) {
    log.info("[SubscriptionService] getMySubscription() - START | userId: {}", user.getId());

    /*
       (1) 구독 조회
       - 회원가입 시점에 AuthServiceImpl이 FREE 기본값 행을 생성하므로 항상 존재해야 한다.
    */
    Subscription subscription =
        subscriptionRepository
            .findByUserId(user.getId())
            .orElseThrow(() -> new CustomException(SubscriptionErrorCode.SUBSCRIPTION_NOT_FOUND));

    /*
       (2) ResponseDto Mapping
    */
    SubscriptionResponse result = subscriptionMapper.toResponse(subscription);

    log.info("[SubscriptionService] getMySubscription() - END | userId: {}", user.getId());
    return result;
  }
}
