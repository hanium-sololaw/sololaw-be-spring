/* 
 * Copyright (c) HANIUM SOLOLAW 
 */
package com.hanium.sololaw.domain.precedentsubscription.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hanium.sololaw.domain.precedentsubscription.dto.response.PrecedentSubscriptionResponse;
import com.hanium.sololaw.domain.precedentsubscription.entity.PrecedentSubscription;
import com.hanium.sololaw.domain.precedentsubscription.exception.PrecedentSubscriptionErrorCode;
import com.hanium.sololaw.domain.precedentsubscription.mapper.PrecedentSubscriptionMapper;
import com.hanium.sololaw.domain.precedentsubscription.repository.PrecedentSubscriptionRepository;
import com.hanium.sololaw.domain.user.entity.User;
import com.hanium.sololaw.global.exception.CustomException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class PrecedentSubscriptionServiceImpl implements PrecedentSubscriptionService {

  private final PrecedentSubscriptionRepository precedentSubscriptionRepository;
  private final PrecedentSubscriptionMapper precedentSubscriptionMapper;

  @Override
  @Transactional(readOnly = true)
  public PrecedentSubscriptionResponse getMyPrecedentSubscription(User user) {
    log.info(
        "[PrecedentSubscriptionService] getMyPrecedentSubscription() - START | userId: {}",
        user.getId());

    /*
       (1) 판례검색 구독 조회
       - 회원가입 시점에 AuthServiceImpl이 FREE 기본값 행을 생성하므로 항상 존재해야 한다.
    */
    PrecedentSubscription precedentSubscription =
        precedentSubscriptionRepository
            .findByUserId(user.getId())
            .orElseThrow(
                () ->
                    new CustomException(
                        PrecedentSubscriptionErrorCode.PRECEDENT_SUBSCRIPTION_NOT_FOUND));

    /*
       (2) ResponseDto Mapping
    */
    PrecedentSubscriptionResponse result =
        precedentSubscriptionMapper.toResponse(precedentSubscription);

    log.info(
        "[PrecedentSubscriptionService] getMyPrecedentSubscription() - END | userId: {}",
        user.getId());
    return result;
  }
}
