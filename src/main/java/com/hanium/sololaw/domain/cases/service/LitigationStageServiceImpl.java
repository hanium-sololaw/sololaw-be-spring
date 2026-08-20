/* 
 * Copyright (c) HANIUM SOLOLAW 
 */
package com.hanium.sololaw.domain.cases.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hanium.sololaw.domain.cases.dto.request.UpdateStageStatusRequest;
import com.hanium.sololaw.domain.cases.dto.response.LitigationStageResponse;
import com.hanium.sololaw.domain.cases.entity.Case;
import com.hanium.sololaw.domain.cases.entity.LitigationStage;
import com.hanium.sololaw.domain.cases.entity.enums.StageStatus;
import com.hanium.sololaw.domain.cases.exception.CaseErrorCode;
import com.hanium.sololaw.domain.cases.mapper.LitigationStageMapper;
import com.hanium.sololaw.domain.cases.repository.CaseRepository;
import com.hanium.sololaw.domain.cases.repository.LitigationStageRepository;
import com.hanium.sololaw.domain.user.entity.User;
import com.hanium.sololaw.global.exception.CustomException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class LitigationStageServiceImpl implements LitigationStageService {

  private final CaseRepository caseRepository;
  private final LitigationStageRepository litigationStageRepository;
  private final LitigationStageMapper litigationStageMapper;

  @Override
  @Transactional(readOnly = true)
  public List<LitigationStageResponse> getStages(User user, Long caseId) {
    log.info(
        "[LitigationStageService] getStages() - START | userId: {}, caseId: {}",
        user.getId(),
        caseId);

    /*
       1. 사건 소유자 검증
    */
    verifyCaseOwnership(caseId, user.getId());

    /*
       2. 절차 단계 목록 조회 및 ResponseDto Mapping
    */
    List<LitigationStageResponse> result =
        litigationStageMapper.toResponseList(
            litigationStageRepository.findAllByCaseIdOrderByStageOrderAsc(caseId));

    log.info(
        "[LitigationStageService] getStages() - END | caseId: {}, count: {}",
        caseId,
        result.size());
    return result;
  }

  @Override
  @Transactional
  public LitigationStageResponse updateStageStatus(
      User user, Long caseId, Long stageId, UpdateStageStatusRequest request) {
    log.info(
        "[LitigationStageService] updateStageStatus() - START | userId: {}, caseId: {}, stageId: {}, status: {}",
        user.getId(),
        caseId,
        stageId,
        request.status());

    /*
       1. 사건 소유자 검증
    */
    Case caseEntity = verifyCaseOwnership(caseId, user.getId());

    /*
       2. 절차 단계 조회 및 상태 변경
    */
    LitigationStage litigationStage =
        litigationStageRepository
            .findByIdAndCaseId(stageId, caseId)
            .orElseThrow(() -> new CustomException(CaseErrorCode.LITIGATION_STAGE_NOT_FOUND));
    litigationStage.updateStatus(request.status());

    /*
       3. 사건 progress_rate 재계산
       - 완료 단계 수 / 전체 단계 수 비율(%)로 서버가 자동 계산한다.
    */
    long totalCount = litigationStageRepository.countByCaseId(caseId);
    long completedCount =
        litigationStageRepository.countByCaseIdAndStatus(caseId, StageStatus.COMPLETED);
    int progressRate = totalCount == 0 ? 0 : (int) Math.round(completedCount * 100.0 / totalCount);
    caseEntity.updateProgressRate(progressRate);

    /*
       4. ResponseDto Mapping
    */
    LitigationStageResponse result = litigationStageMapper.toResponse(litigationStage);

    log.info(
        "[LitigationStageService] updateStageStatus() - END | stageId: {}, progressRate: {}",
        stageId,
        progressRate);
    return result;
  }

  private Case verifyCaseOwnership(Long caseId, Long userId) {
    return caseRepository
        .findByIdAndUserId(caseId, userId)
        .orElseThrow(() -> new CustomException(CaseErrorCode.CASE_NOT_FOUND));
  }
}
