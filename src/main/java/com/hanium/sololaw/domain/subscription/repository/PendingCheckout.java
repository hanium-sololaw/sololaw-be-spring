/* 
 * Copyright (c) HANIUM SOLOLAW 
 */
package com.hanium.sololaw.domain.subscription.repository;

import com.hanium.sololaw.domain.subscription.entity.enums.StoragePlan;

/**
 * checkout()에서 confirm()까지 이어지는 결제 대기 세션. 클라이언트가 위조한 플랜·금액으로 confirm하는 것을 막기 위해 Redis에 서버 기준값을 둔다.
 */
public record PendingCheckout(Long userId, StoragePlan plan, long amount) {}
