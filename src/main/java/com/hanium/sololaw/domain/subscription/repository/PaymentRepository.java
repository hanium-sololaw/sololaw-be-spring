/* 
 * Copyright (c) HANIUM SOLOLAW 
 */
package com.hanium.sololaw.domain.subscription.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.hanium.sololaw.domain.subscription.entity.Payment;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

  Optional<Payment> findByProviderPaymentKey(String providerPaymentKey);

  Page<Payment> findAllByUserIdOrderByPaidAtDesc(Long userId, Pageable pageable);
}
