/* 
 * Copyright (c) HANIUM SOLOLAW 
 */
package com.hanium.sololaw.domain.precedentsubscription.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.hanium.sololaw.domain.precedentsubscription.dto.response.PrecedentSubscriptionResponse;
import com.hanium.sololaw.domain.precedentsubscription.entity.PrecedentSubscription;
import com.hanium.sololaw.domain.precedentsubscription.entity.enums.PrecedentSearchPlan;
import com.hanium.sololaw.domain.precedentsubscription.exception.PrecedentSubscriptionErrorCode;
import com.hanium.sololaw.domain.precedentsubscription.mapper.PrecedentSubscriptionMapper;
import com.hanium.sololaw.domain.precedentsubscription.repository.PrecedentSubscriptionRepository;
import com.hanium.sololaw.domain.subscription.entity.enums.SubscriptionStatus;
import com.hanium.sololaw.domain.user.entity.User;
import com.hanium.sololaw.global.exception.CustomException;

@ExtendWith(MockitoExtension.class)
class PrecedentSubscriptionServiceTest {

  @Mock private PrecedentSubscriptionRepository precedentSubscriptionRepository;
  @Mock private PrecedentSubscriptionMapper precedentSubscriptionMapper;

  @InjectMocks private PrecedentSubscriptionServiceImpl precedentSubscriptionService;

  @Test
  void getMyPrecedentSubscription_returnsMappedResponse_whenFound() {
    User user = User.builder().id(1L).build();
    PrecedentSubscription precedentSubscription =
        PrecedentSubscription.builder()
            .id(10L)
            .userId(1L)
            .plan(PrecedentSearchPlan.FREE)
            .status(SubscriptionStatus.ACTIVE)
            .build();
    PrecedentSubscriptionResponse expected =
        PrecedentSubscriptionResponse.builder()
            .plan(PrecedentSearchPlan.FREE)
            .status(SubscriptionStatus.ACTIVE)
            .build();
    when(precedentSubscriptionRepository.findByUserId(1L))
        .thenReturn(Optional.of(precedentSubscription));
    when(precedentSubscriptionMapper.toResponse(precedentSubscription)).thenReturn(expected);

    PrecedentSubscriptionResponse result =
        precedentSubscriptionService.getMyPrecedentSubscription(user);

    assertThat(result).isEqualTo(expected);
  }

  @Test
  void getMyPrecedentSubscription_throwsNotFound_whenMissing() {
    User user = User.builder().id(1L).build();
    when(precedentSubscriptionRepository.findByUserId(1L)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> precedentSubscriptionService.getMyPrecedentSubscription(user))
        .isInstanceOf(CustomException.class)
        .extracting(e -> ((CustomException) e).getErrorCode())
        .isEqualTo(PrecedentSubscriptionErrorCode.PRECEDENT_SUBSCRIPTION_NOT_FOUND);
  }
}
