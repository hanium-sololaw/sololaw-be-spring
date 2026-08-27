/* 
 * Copyright (c) HANIUM SOLOLAW 
 */
package com.hanium.sololaw.domain.cases.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.hanium.sololaw.domain.cases.dto.request.CreatePartyRequest;
import com.hanium.sololaw.domain.cases.dto.request.UpdatePartyRequest;
import com.hanium.sololaw.domain.cases.dto.response.CasePartyResponse;
import com.hanium.sololaw.domain.cases.entity.Case;
import com.hanium.sololaw.domain.cases.entity.CaseParty;
import com.hanium.sololaw.domain.cases.entity.enums.PartyRole;
import com.hanium.sololaw.domain.cases.exception.CaseErrorCode;
import com.hanium.sololaw.domain.cases.mapper.CasePartyMapper;
import com.hanium.sololaw.domain.cases.repository.CasePartyRepository;
import com.hanium.sololaw.domain.cases.repository.CaseRepository;
import com.hanium.sololaw.domain.user.entity.User;
import com.hanium.sololaw.global.crypto.AesEncryptor;
import com.hanium.sololaw.global.exception.CustomException;

@ExtendWith(MockitoExtension.class)
class CasePartyServiceTest {

  @Mock private CaseRepository caseRepository;
  @Mock private CasePartyRepository casePartyRepository;
  @Mock private CasePartyMapper casePartyMapper;
  @Mock private AesEncryptor aesEncryptor;

  @InjectMocks private CasePartyServiceImpl casePartyService;

  @Test
  void updatePartyDetails_encryptsResidentNoBeforeSaving() {
    User user = User.builder().id(1L).build();
    Case ownedCase = Case.builder().id(5L).userId(1L).build();
    CaseParty caseParty =
        CaseParty.builder().id(10L).caseId(5L).partyRole(PartyRole.PLAINTIFF).name("김철수").build();
    UpdatePartyRequest request =
        new UpdatePartyRequest(
            "990101-1234567",
            "서울시",
            "010-1234-5678",
            "user@example.com",
            "서울시 송달주소",
            "02-1234-5678",
            "대표이사 박대표");

    when(caseRepository.findByIdAndUserId(5L, 1L)).thenReturn(Optional.of(ownedCase));
    when(casePartyRepository.findByIdAndCaseId(10L, 5L)).thenReturn(Optional.of(caseParty));
    when(aesEncryptor.encrypt("990101-1234567")).thenReturn("ENCRYPTED");
    when(casePartyMapper.toResponse(caseParty))
        .thenReturn(CasePartyResponse.builder().id(10L).build());

    casePartyService.updatePartyDetails(user, 5L, 10L, request);

    assertThat(caseParty.getResidentNo()).isEqualTo("ENCRYPTED");
    assertThat(caseParty.getAddress()).isEqualTo("서울시");
    assertThat(caseParty.getPhone()).isEqualTo("010-1234-5678");
    assertThat(caseParty.getEmail()).isEqualTo("user@example.com");
    assertThat(caseParty.getServiceAddress()).isEqualTo("서울시 송달주소");
    assertThat(caseParty.getFax()).isEqualTo("02-1234-5678");
    assertThat(caseParty.getRepresentative()).isEqualTo("대표이사 박대표");
  }

  @Test
  void updatePartyDetails_keepsExistingValues_whenNewFieldsNull() {
    User user = User.builder().id(1L).build();
    Case ownedCase = Case.builder().id(5L).userId(1L).build();
    CaseParty caseParty =
        CaseParty.builder()
            .id(10L)
            .caseId(5L)
            .partyRole(PartyRole.DEFENDANT)
            .name("주식회사 테스트")
            .email("existing@example.com")
            .serviceAddress("기존 송달주소")
            .fax("02-0000-0000")
            .representative("기존대표")
            .build();
    UpdatePartyRequest request = new UpdatePartyRequest(null, null, null, null, null, null, null);

    when(caseRepository.findByIdAndUserId(5L, 1L)).thenReturn(Optional.of(ownedCase));
    when(casePartyRepository.findByIdAndCaseId(10L, 5L)).thenReturn(Optional.of(caseParty));
    when(casePartyMapper.toResponse(caseParty))
        .thenReturn(CasePartyResponse.builder().id(10L).build());

    casePartyService.updatePartyDetails(user, 5L, 10L, request);

    assertThat(caseParty.getEmail()).isEqualTo("existing@example.com");
    assertThat(caseParty.getServiceAddress()).isEqualTo("기존 송달주소");
    assertThat(caseParty.getFax()).isEqualTo("02-0000-0000");
    assertThat(caseParty.getRepresentative()).isEqualTo("기존대표");
  }

  @Test
  void addParty_throwsNotFound_whenCaseNotOwned() {
    User user = User.builder().id(1L).build();
    CreatePartyRequest request = new CreatePartyRequest(PartyRole.THIRD_PARTY, "박영수");
    when(caseRepository.findByIdAndUserId(999L, 1L)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> casePartyService.addParty(user, 999L, request))
        .isInstanceOf(CustomException.class)
        .extracting(e -> ((CustomException) e).getErrorCode())
        .isEqualTo(CaseErrorCode.CASE_NOT_FOUND);
  }

  @Test
  void updatePartyDetails_throwsNotFound_whenPartyMissing() {
    User user = User.builder().id(1L).build();
    Case ownedCase = Case.builder().id(5L).userId(1L).build();
    UpdatePartyRequest request = new UpdatePartyRequest(null, null, null, null, null, null, null);
    when(caseRepository.findByIdAndUserId(5L, 1L)).thenReturn(Optional.of(ownedCase));
    when(casePartyRepository.findByIdAndCaseId(999L, 5L)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> casePartyService.updatePartyDetails(user, 5L, 999L, request))
        .isInstanceOf(CustomException.class)
        .extracting(e -> ((CustomException) e).getErrorCode())
        .isEqualTo(CaseErrorCode.CASE_PARTY_NOT_FOUND);
  }
}
