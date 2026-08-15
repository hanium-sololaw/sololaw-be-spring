/* 
 * Copyright (c) HANIUM SOLOLAW 
 */
package com.hanium.sololaw.domain.cases.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.*;

import com.hanium.sololaw.domain.cases.entity.enums.CaseStatus;
import com.hanium.sololaw.domain.cases.entity.enums.CaseType;
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
@Table(name = "cases")
public class Case extends BaseTimeEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "user_id", nullable = false)
  private Long userId;

  @Column(length = 50)
  private String caseNumber;

  @Column(nullable = false, length = 200)
  private String title;

  @Enumerated(EnumType.STRING)
  @Column(length = 20)
  private CaseType caseType;

  @Builder.Default
  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private CaseStatus status = CaseStatus.PREPARING;

  @Builder.Default
  @Column(nullable = false)
  private Integer progressRate = 0;

  @Column(length = 200)
  private String court;

  @Column(precision = 15, scale = 0)
  private BigDecimal claimAmount;

  private LocalDateTime openedAt;

  public void update(
      String title, CaseType caseType, BigDecimal claimAmount, String court, String caseNumber) {
    this.title = title;
    this.caseType = caseType;
    this.claimAmount = claimAmount;
    this.court = court;
    this.caseNumber = caseNumber;
  }

  public void updateStatus(CaseStatus status) {
    this.status = status;
  }

  public void updateProgressRate(int progressRate) {
    this.progressRate = progressRate;
  }
}
