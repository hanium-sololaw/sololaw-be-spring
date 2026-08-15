/* 
 * Copyright (c) HANIUM SOLOLAW 
 */
package com.hanium.sololaw.domain.precedentsubscription.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.*;

import com.hanium.sololaw.domain.precedentsubscription.entity.enums.PrecedentSearchPlan;
import com.hanium.sololaw.domain.subscription.entity.enums.BillingCycle;
import com.hanium.sololaw.domain.subscription.entity.enums.SubscriptionStatus;
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
@Table(name = "precedent_subscriptions")
public class PrecedentSubscription extends BaseTimeEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "user_id", nullable = false, unique = true)
  private Long userId;

  @Builder.Default
  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private PrecedentSearchPlan plan = PrecedentSearchPlan.FREE;

  @Builder.Default
  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private SubscriptionStatus status = SubscriptionStatus.ACTIVE;

  @Builder.Default
  @Column(nullable = false, precision = 15, scale = 0)
  private BigDecimal priceKrw = BigDecimal.ZERO;

  @Enumerated(EnumType.STRING)
  @Column(length = 20)
  private BillingCycle billingCycle;

  private LocalDateTime nextBillingAt;

  private LocalDateTime canceledAt;

  @Column(length = 255)
  private String stripeCustomerId;

  @Column(length = 255)
  private String stripeSubscriptionId;

  @Column(nullable = false)
  private LocalDateTime startedAt;

  private LocalDateTime expiresAt;

  /**
   * FREE 플랜 기본값으로 판례검색 구독을 생성합니다. userId·startedAt 외 필드는 이 엔티티의 {@code @Builder.Default} 값을 따릅니다.
   *
   * @param userId 구독을 생성할 사용자 ID
   * @return 기본값으로 구성된 PrecedentSubscription
   */
  public static PrecedentSubscription createDefault(Long userId) {
    return PrecedentSubscription.builder().userId(userId).startedAt(LocalDateTime.now()).build();
  }
}
