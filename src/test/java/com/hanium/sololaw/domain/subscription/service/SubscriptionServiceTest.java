/* 
 * Copyright (c) HANIUM SOLOLAW 
 */
package com.hanium.sololaw.domain.subscription.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.hanium.sololaw.domain.subscription.dto.response.SubscriptionResponse;
import com.hanium.sololaw.domain.subscription.entity.Subscription;
import com.hanium.sololaw.domain.subscription.entity.enums.SubscriptionPlan;
import com.hanium.sololaw.domain.subscription.entity.enums.SubscriptionStatus;
import com.hanium.sololaw.domain.subscription.exception.SubscriptionErrorCode;
import com.hanium.sololaw.domain.subscription.mapper.SubscriptionMapper;
import com.hanium.sololaw.domain.subscription.repository.SubscriptionRepository;
import com.hanium.sololaw.domain.user.entity.User;
import com.hanium.sololaw.global.exception.CustomException;

@ExtendWith(MockitoExtension.class)
class SubscriptionServiceTest {

  @Mock private SubscriptionRepository subscriptionRepository;
  @Mock private SubscriptionMapper subscriptionMapper;

  @InjectMocks private SubscriptionServiceImpl subscriptionService;

  @Test
  void getMySubscription_returnsMappedResponse_whenFound() {
    User user = User.builder().id(1L).build();
    Subscription subscription =
        Subscription.builder()
            .id(10L)
            .userId(1L)
            .plan(SubscriptionPlan.FREE)
            .status(SubscriptionStatus.ACTIVE)
            .build();
    SubscriptionResponse expected =
        SubscriptionResponse.builder()
            .plan(SubscriptionPlan.FREE)
            .status(SubscriptionStatus.ACTIVE)
            .build();
    when(subscriptionRepository.findByUserId(1L)).thenReturn(Optional.of(subscription));
    when(subscriptionMapper.toResponse(subscription)).thenReturn(expected);

    SubscriptionResponse result = subscriptionService.getMySubscription(user);

    assertThat(result).isEqualTo(expected);
  }

  @Test
  void getMySubscription_throwsNotFound_whenMissing() {
    User user = User.builder().id(1L).build();
    when(subscriptionRepository.findByUserId(1L)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> subscriptionService.getMySubscription(user))
        .isInstanceOf(CustomException.class)
        .extracting(e -> ((CustomException) e).getErrorCode())
        .isEqualTo(SubscriptionErrorCode.SUBSCRIPTION_NOT_FOUND);
  }
}
