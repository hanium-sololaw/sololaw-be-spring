/* 
 * Copyright (c) HANIUM SOLOLAW 
 */
package com.hanium.sololaw.domain.cases.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hanium.sololaw.domain.cases.dto.request.CreatePartyRequest;
import com.hanium.sololaw.domain.cases.dto.request.UpdatePartyRequest;
import com.hanium.sololaw.domain.cases.dto.response.CasePartyResponse;
import com.hanium.sololaw.domain.cases.entity.CaseParty;
import com.hanium.sololaw.domain.cases.exception.CaseErrorCode;
import com.hanium.sololaw.domain.cases.mapper.CasePartyMapper;
import com.hanium.sololaw.domain.cases.repository.CasePartyRepository;
import com.hanium.sololaw.domain.cases.repository.CaseRepository;
import com.hanium.sololaw.domain.user.entity.User;
import com.hanium.sololaw.global.crypto.AesEncryptor;
import com.hanium.sololaw.global.exception.CustomException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class CasePartyServiceImpl implements CasePartyService {

  private final CaseRepository caseRepository;
  private final CasePartyRepository casePartyRepository;
  private final CasePartyMapper casePartyMapper;
  private final AesEncryptor aesEncryptor;

  @Override
  @Transactional(readOnly = true)
  public List<CasePartyResponse> getParties(User user, Long caseId) {
    log.info(
        "[CasePartyService] getParties() - START | userId: {}, caseId: {}", user.getId(), caseId);

    /*
       1. 사건 소유자 검증
    */
    verifyCaseOwnership(caseId, user.getId());

    /*
       2. 당사자 목록 조회 및 ResponseDto Mapping
    */
    List<CasePartyResponse> result =
        casePartyMapper.toResponseList(casePartyRepository.findAllByCaseId(caseId));

    log.info(
        "[CasePartyService] getParties() - END | caseId: {}, count: {}", caseId, result.size());
    return result;
  }

  @Override
  @Transactional
  public CasePartyResponse addParty(User user, Long caseId, CreatePartyRequest request) {
    log.info(
        "[CasePartyService] addParty() - START | userId: {}, caseId: {}, partyRole: {}",
        user.getId(),
        caseId,
        request.partyRole());

    /*
       1. 사건 소유자 검증
    */
    verifyCaseOwnership(caseId, user.getId());

    /*
       2. 당사자 생성 및 저장
    */
    CaseParty savedParty = casePartyRepository.save(casePartyMapper.toEntity(caseId, request));

    /*
       3. ResponseDto Mapping
    */
    CasePartyResponse result = casePartyMapper.toResponse(savedParty);

    log.info("[CasePartyService] addParty() - END | partyId: {}", savedParty.getId());
    return result;
  }

  @Override
  @Transactional
  public CasePartyResponse updatePartyDetails(
      User user, Long caseId, Long partyId, UpdatePartyRequest request) {
    log.info(
        "[CasePartyService] updatePartyDetails() - START | userId: {}, caseId: {}, partyId: {}",
        user.getId(),
        caseId,
        partyId);

    /*
       1. 사건 소유자 검증
    */
    verifyCaseOwnership(caseId, user.getId());

    /*
       2. 당사자 조회
    */
    CaseParty caseParty =
        casePartyRepository
            .findByIdAndCaseId(partyId, caseId)
            .orElseThrow(() -> new CustomException(CaseErrorCode.CASE_PARTY_NOT_FOUND));

    /*
       3. 상세정보 수정
       - residentNo는 AES 암호화 후 저장한다(평문 저장 금지). null인 필드는 기존 값을 유지한다.
    */
    String encryptedResidentNo =
        request.residentNo() != null
            ? aesEncryptor.encrypt(request.residentNo())
            : caseParty.getResidentNo();
    caseParty.updateDetails(
        encryptedResidentNo,
        request.address() != null ? request.address() : caseParty.getAddress(),
        request.phone() != null ? request.phone() : caseParty.getPhone(),
        request.email() != null ? request.email() : caseParty.getEmail(),
        request.serviceAddress() != null ? request.serviceAddress() : caseParty.getServiceAddress(),
        request.fax() != null ? request.fax() : caseParty.getFax(),
        request.representative() != null
            ? request.representative()
            : caseParty.getRepresentative());

    /*
       4. ResponseDto Mapping
    */
    CasePartyResponse result = casePartyMapper.toResponse(caseParty);

    log.info("[CasePartyService] updatePartyDetails() - END | partyId: {}", partyId);
    return result;
  }

  @Override
  @Transactional
  public void deleteParty(User user, Long caseId, Long partyId) {
    log.info(
        "[CasePartyService] deleteParty() - START | userId: {}, caseId: {}, partyId: {}",
        user.getId(),
        caseId,
        partyId);

    /*
       1. 사건 소유자 검증
    */
    verifyCaseOwnership(caseId, user.getId());

    /*
       2. 당사자 조회 및 삭제
    */
    CaseParty caseParty =
        casePartyRepository
            .findByIdAndCaseId(partyId, caseId)
            .orElseThrow(() -> new CustomException(CaseErrorCode.CASE_PARTY_NOT_FOUND));
    casePartyRepository.delete(caseParty);

    log.info("[CasePartyService] deleteParty() - END | partyId: {}", partyId);
  }

  private void verifyCaseOwnership(Long caseId, Long userId) {
    caseRepository
        .findByIdAndUserId(caseId, userId)
        .orElseThrow(() -> new CustomException(CaseErrorCode.CASE_NOT_FOUND));
  }
}
