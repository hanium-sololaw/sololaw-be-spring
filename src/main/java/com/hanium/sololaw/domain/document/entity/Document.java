/* 
 * Copyright (c) HANIUM SOLOLAW 
 */
package com.hanium.sololaw.domain.document.entity;

import java.time.LocalDateTime;

import jakarta.persistence.*;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import com.hanium.sololaw.domain.document.entity.enums.ApplicationSubtype;
import com.hanium.sololaw.domain.document.entity.enums.DocType;
import com.hanium.sololaw.domain.document.entity.enums.DocumentStatus;
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
@Table(name = "documents")
public class Document extends BaseTimeEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "case_id", nullable = false)
  private Long caseId;

  @Column(name = "user_id", nullable = false)
  private Long userId;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private DocType docType;

  @Enumerated(EnumType.STRING)
  @Column(length = 20)
  private ApplicationSubtype applicationSubtype;

  @Column(length = 200)
  private String title;

  @Builder.Default
  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private DocumentStatus status = DocumentStatus.DRAFT;

  @Builder.Default
  @Column(nullable = false)
  private Boolean isLatest = true;

  @Column(length = 500)
  private String fileUrl;

  @JdbcTypeCode(SqlTypes.JSON)
  private String content;

  @JdbcTypeCode(SqlTypes.JSON)
  private String generatedContent;

  @Column(columnDefinition = "TEXT")
  private String generatedText;

  private LocalDateTime generatedAt;

  public void updateDraft(String title, String content) {
    this.title = title;
    this.content = content;
  }

  public void updateIsLatest(boolean isLatest) {
    this.isLatest = isLatest;
  }

  public void saveResult(String generatedContent, String generatedText, String fileUrl) {
    this.generatedContent = generatedContent;
    this.generatedText = generatedText;
    this.fileUrl = fileUrl;
    this.generatedAt = LocalDateTime.now();
  }

  public void updateStatus(DocumentStatus status) {
    this.status = status;
  }
}
