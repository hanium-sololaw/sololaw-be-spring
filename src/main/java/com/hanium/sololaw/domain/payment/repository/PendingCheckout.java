/* 
 * Copyright (c) HANIUM SOLOLAW 
 */
package com.hanium.sololaw.domain.payment.repository;

import com.hanium.sololaw.domain.payment.entity.enums.SubscriptionType;

/**
 * checkout()에서 confirm()까지 이어지는 결제 대기 세션. 클라이언트가 위조한 구독 종류·플랜·금액으로 confirm하는 것을 막기 위해 Redis에 서버
 * 기준값을 둔다. planCode는 StoragePlan·PrecedentSearchPlan 등 구독 종류별 enum의 name()이다.
 */
public record PendingCheckout(
    Long userId, SubscriptionType subscriptionType, String planCode, long amount) {}
