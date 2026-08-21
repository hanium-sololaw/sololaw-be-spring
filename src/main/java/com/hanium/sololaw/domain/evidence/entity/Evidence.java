/* 
 * Copyright (c) HANIUM SOLOLAW 
 */
package com.hanium.sololaw.domain.evidence.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;

import jakarta.persistence.*;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import com.hanium.sololaw.domain.evidence.entity.enums.EvidenceStatus;
import com.hanium.sololaw.domain.evidence.entity.enums.ExhibitParty;
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
@Table(name = "evidence")
public class Evidence extends BaseTimeEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "case_id", nullable = false)
  private Long caseId;

  @Column(name = "folder_id")
  private Long folderId;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private ExhibitParty partyType;

  @Column(length = 50)
  private String exhibitNo;

  @Column(nullable = false, length = 255)
  private String fileName;

  @Column(length = 500)
  private String fileUrl;

  private Long fileSize;

  @Column(length = 20)
  private String fileType;

  @Builder.Default
  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private EvidenceStatus status = EvidenceStatus.PENDING;

  @Column(columnDefinition = "TEXT")
  private String proofPurpose;

  @Column(columnDefinition = "TEXT")
  private String description;

  @JdbcTypeCode(SqlTypes.ARRAY)
  @Column(columnDefinition = "text[]")
  private String[] tags;

  private LocalDateTime submittedAt;

  private LocalDate deadline;

  private LocalDateTime uploadedAt;

  public void update(
      String exhibitNo,
      String proofPurpose,
      String description,
      String[] tags,
      LocalDate deadline) {
    this.exhibitNo = exhibitNo;
    this.proofPurpose = proofPurpose;
    this.description = description;
    this.tags = tags;
    this.deadline = deadline;
  }

  public void updateStatus(EvidenceStatus status) {
    this.status = status;
    if (status == EvidenceStatus.SUBMITTED) {
      this.submittedAt = LocalDateTime.now();
    }
  }
}
