/* 
 * Copyright (c) HANIUM SOLOLAW 
 */
package com.hanium.sololaw.domain.subscription.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.*;

import com.hanium.sololaw.domain.subscription.entity.enums.PaymentProvider;
import com.hanium.sololaw.domain.subscription.entity.enums.PaymentStatus;
import com.hanium.sololaw.domain.subscription.entity.enums.StoragePlan;
import com.hanium.sololaw.global.common.BaseTimeEntity;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "payments")
public class Payment extends BaseTimeEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "user_id", nullable = false)
  private Long userId;

  /** 환불 등으로 구독이 삭제돼도 결제 이력은 남아야 하므로 FK를 걸지 않는다. */
  @Column(name = "subscription_id")
  private Long subscriptionId;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private StoragePlan plan;

  @Column(nullable = false, precision = 15, scale = 0)
  private BigDecimal amount;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private PaymentProvider provider;

  @Column(nullable = false, unique = true, length = 255)
  private String providerPaymentKey;

  @Column(nullable = false, length = 255)
  private String providerOrderId;

  @Builder.Default
  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private PaymentStatus status = PaymentStatus.PAID;

  @Column(nullable = false)
  private LocalDateTime paidAt;

  @Column(length = 500)
  private String receiptUrl;

  public void refund() {
    this.status = PaymentStatus.REFUNDED;
  }
}
