/* 
 * Copyright (c) HANIUM SOLOLAW 
 */
package com.hanium.sololaw.domain.precedentsubscription.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.hanium.sololaw.domain.precedentsubscription.entity.PrecedentSubscription;

public interface PrecedentSubscriptionRepository
    extends JpaRepository<PrecedentSubscription, Long> {

  Optional<PrecedentSubscription> findByUserId(Long userId);
}
