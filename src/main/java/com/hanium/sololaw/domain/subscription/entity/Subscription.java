/* 
 * Copyright (c) HANIUM SOLOLAW 
 */
package com.hanium.sololaw.domain.subscription.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.*;

import com.hanium.sololaw.domain.subscription.entity.enums.BillingCycle;
import com.hanium.sololaw.domain.subscription.entity.enums.StoragePlan;
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
@Table(name = "subscriptions")
public class Subscription extends BaseTimeEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "user_id", nullable = false, unique = true)
  private Long userId;

  @Builder.Default
  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private StoragePlan plan = StoragePlan.FREE;

  @Builder.Default
  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private SubscriptionStatus status = SubscriptionStatus.ACTIVE;

  @Builder.Default
  @Column(nullable = false)
  private Long storageLimitBytes = 524_288_000L;

  @Builder.Default
  @Column(nullable = false)
  private Long usedStorageBytes = 0L;

  @Builder.Default
  @Column(nullable = false, precision = 15, scale = 0)
  private BigDecimal priceKrw = BigDecimal.ZERO;

  @Enumerated(EnumType.STRING)
  @Column(length = 20)
  private BillingCycle billingCycle;

  private LocalDateTime nextBillingAt;

  private LocalDateTime canceledAt;

  @Column(nullable = false)
  private LocalDateTime startedAt;

  private LocalDateTime expiresAt;

  /**
   * FREE 플랜 기본값으로 구독을 생성합니다. userId·startedAt 외 필드는 이 엔티티의 {@code @Builder.Default} 값을 따릅니다.
   *
   * @param userId 구독을 생성할 사용자 ID
   * @return 기본값으로 구성된 Subscription
   */
  public static Subscription createDefault(Long userId) {
    return Subscription.builder().userId(userId).startedAt(LocalDateTime.now()).build();
  }

  /**
   * 결제 승인 후 유료 플랜을 활성화합니다. 해지 이력이 있어도 재구독이므로 canceledAt을 초기화합니다.
   *
   * @param plan 활성화할 플랜
   * @param nextBillingAt 다음 결제 예정일
   */
  public void activatePaidPlan(StoragePlan plan, LocalDateTime nextBillingAt) {
    this.plan = plan;
    this.status = SubscriptionStatus.ACTIVE;
    this.priceKrw = BigDecimal.valueOf(plan.getPriceKrw());
    this.storageLimitBytes = plan.getStorageLimitBytes();
    this.billingCycle = BillingCycle.MONTHLY;
    this.nextBillingAt = nextBillingAt;
    this.canceledAt = null;
  }

  /** 구독을 해지합니다. 다음 결제 주기부터 갱신되지 않으며, 현재 플랜은 만료 전까지 그대로 유지됩니다. */
  public void cancel() {
    this.status = SubscriptionStatus.CANCELED;
    this.canceledAt = LocalDateTime.now();
  }
}
