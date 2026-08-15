/* 
 * Copyright (c) HANIUM SOLOLAW 
 */
package com.hanium.sololaw.domain.cases.entity;

import java.time.LocalDate;

import jakarta.persistence.*;

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
@Table(name = "case_todos")
public class CaseTodo extends BaseTimeEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "case_id", nullable = false)
  private Long caseId;

  @Column(nullable = false, length = 200)
  private String title;

  private LocalDate dueDate;

  @Builder.Default
  @Column(nullable = false)
  private Boolean isDone = false;

  public void update(String title, LocalDate dueDate) {
    this.title = title;
    this.dueDate = dueDate;
  }

  public void updateIsDone(boolean isDone) {
    this.isDone = isDone;
  }
}
