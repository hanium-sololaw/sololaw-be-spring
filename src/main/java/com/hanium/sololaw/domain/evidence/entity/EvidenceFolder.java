/* 
 * Copyright (c) HANIUM SOLOLAW 
 */
package com.hanium.sololaw.domain.evidence.entity;

import jakarta.persistence.*;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

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
@Table(name = "evidence_folders")
public class EvidenceFolder extends BaseTimeEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "user_id", nullable = false)
  private Long userId;

  @Column(name = "case_id")
  private Long caseId;

  @Column(nullable = false, length = 100)
  private String name;

  @Column(length = 50)
  private String folderType;

  @JdbcTypeCode(SqlTypes.ARRAY)
  @Column(columnDefinition = "text[]")
  private String[] tags;

  public void update(String name, String folderType, String[] tags) {
    this.name = name;
    this.folderType = folderType;
    this.tags = tags;
  }
}
