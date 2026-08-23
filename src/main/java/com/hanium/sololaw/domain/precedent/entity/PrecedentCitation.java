/* 
 * Copyright (c) HANIUM SOLOLAW 
 */
package com.hanium.sololaw.domain.precedent.entity;

import java.time.LocalDateTime;

import jakarta.persistence.*;

import com.hanium.sololaw.domain.precedent.entity.enums.LegalCategory;
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
@Table(name = "precedent_citations")
public class PrecedentCitation extends BaseTimeEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "user_id", nullable = false)
  private Long userId;

  @Column(name = "serial_id", nullable = false, length = 100)
  private String serialId;

  @Column(nullable = false, length = 300)
  private String name;

  @Column(length = 100)
  private String court;

  @Column(name = "decision_date", length = 50)
  private String decisionDate;

  @Enumerated(EnumType.STRING)
  @Column(length = 20)
  private LegalCategory category;

  @Column(name = "reference_note", columnDefinition = "TEXT")
  private String referenceNote;

  @Column(name = "detail_url", length = 500)
  private String detailUrl;

  @Column(name = "case_id")
  private Long caseId;

  @Column(name = "document_id")
  private Long documentId;

  @Column(name = "cited_at", nullable = false)
  private LocalDateTime citedAt;

  public void linkDocument(Long documentId) {
    this.documentId = documentId;
  }
}
