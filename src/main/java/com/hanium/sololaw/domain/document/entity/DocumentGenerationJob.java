/* 
 * Copyright (c) HANIUM SOLOLAW 
 */
package com.hanium.sololaw.domain.document.entity;

import java.time.LocalDateTime;

import jakarta.persistence.*;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import com.hanium.sololaw.domain.document.entity.enums.JobStatus;
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
@Table(name = "document_generation_jobs")
public class DocumentGenerationJob extends BaseTimeEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "document_id", nullable = false)
  private Long documentId;

  @Column(name = "user_id", nullable = false)
  private Long userId;

  @Builder.Default
  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private JobStatus status = JobStatus.PENDING;

  @Builder.Default
  @Column(nullable = false)
  private Integer progress = 0;

  @JdbcTypeCode(SqlTypes.JSON)
  private String requestContent;

  private LocalDateTime failedAt;

  @Column(length = 50)
  private String errorCode;

  @Column(columnDefinition = "TEXT")
  private String errorMessage;

  public void markSucceeded() {
    this.status = JobStatus.SUCCEEDED;
  }
}
