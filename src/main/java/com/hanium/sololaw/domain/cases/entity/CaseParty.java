/* 
 * Copyright (c) HANIUM SOLOLAW 
 */
package com.hanium.sololaw.domain.cases.entity;

import jakarta.persistence.*;

import com.hanium.sololaw.domain.cases.entity.enums.PartyRole;
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
@Table(name = "case_parties")
public class CaseParty extends BaseTimeEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "case_id", nullable = false)
  private Long caseId;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private PartyRole partyRole;

  @Column(nullable = false, length = 100)
  private String name;

  @Column(length = 255)
  private String residentNo;

  @Column(length = 500)
  private String address;

  @Column(length = 20)
  private String phone;

  @Builder.Default
  @Column(nullable = false)
  private Boolean isSelf = false;

  public void updateDetails(String residentNo, String address, String phone) {
    this.residentNo = residentNo;
    this.address = address;
    this.phone = phone;
  }
}
