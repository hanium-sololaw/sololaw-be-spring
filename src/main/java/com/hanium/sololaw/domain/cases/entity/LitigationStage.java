/* 
 * Copyright (c) HANIUM SOLOLAW 
 */
package com.hanium.sololaw.domain.cases.entity;

import java.time.LocalDate;

import jakarta.persistence.*;

import com.hanium.sololaw.domain.cases.entity.enums.StageStatus;
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
@Table(name = "litigation_stages")
public class LitigationStage extends BaseTimeEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "case_id", nullable = false)
  private Long caseId;

  @Column(nullable = false)
  private Integer stageOrder;

  @Column(nullable = false, length = 100)
  private String name;

  @Builder.Default
  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private StageStatus status = StageStatus.SCHEDULED;

  private LocalDate stageDate;

  @Column(columnDefinition = "TEXT")
  private String description;

  public void updateStatus(StageStatus status) {
    this.status = status;
  }
}
