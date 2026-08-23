/* 
 * Copyright (c) HANIUM SOLOLAW 
 */
package com.hanium.sololaw.domain.precedent.entity;

import jakarta.persistence.*;

import com.hanium.sololaw.domain.precedent.entity.enums.LegalCategory;
import com.hanium.sololaw.domain.precedent.entity.enums.PrecedentOutcome;
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
@Table(name = "precedent_bookmarks")
public class PrecedentBookmark extends BaseTimeEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "user_id", nullable = false)
  private Long userId;

  @Column(name = "serial_id", nullable = false, length = 100)
  private String serialId;

  @Column(nullable = false, length = 300)
  private String name;

  @Column(name = "case_no", nullable = false, length = 100)
  private String caseNo;

  @Column(length = 100)
  private String court;

  @Column(name = "decision_date", length = 50)
  private String decisionDate;

  @Enumerated(EnumType.STRING)
  @Column(length = 10)
  private PrecedentOutcome outcome;

  @Enumerated(EnumType.STRING)
  @Column(length = 20)
  private LegalCategory category;

  @Column(name = "reference_note", columnDefinition = "TEXT")
  private String referenceNote;

  @Column(name = "detail_url", length = 500)
  private String detailUrl;
}
