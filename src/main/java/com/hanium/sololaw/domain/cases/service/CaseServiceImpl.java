/* 
 * Copyright (c) HANIUM SOLOLAW 
 */
package com.hanium.sololaw.domain.cases.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hanium.sololaw.domain.cases.dto.request.CreateCaseRequest;
import com.hanium.sololaw.domain.cases.dto.request.UpdateCaseRequest;
import com.hanium.sololaw.domain.cases.dto.request.UpdateCaseStatusRequest;
import com.hanium.sololaw.domain.cases.dto.response.CaseDetailResponse;
import com.hanium.sololaw.domain.cases.dto.response.CasePartySummaryResponse;
import com.hanium.sololaw.domain.cases.dto.response.CaseResponse;
import com.hanium.sololaw.domain.cases.entity.Case;
import com.hanium.sololaw.domain.cases.entity.LitigationStage;
import com.hanium.sololaw.domain.cases.entity.enums.CaseStatus;
import com.hanium.sololaw.domain.cases.entity.enums.CaseType;
import com.hanium.sololaw.domain.cases.entity.enums.StageStatus;
import com.hanium.sololaw.domain.cases.entity.enums.StartingStage;
import com.hanium.sololaw.domain.cases.exception.CaseErrorCode;
import com.hanium.sololaw.domain.cases.mapper.CaseMapper;
import com.hanium.sololaw.domain.cases.mapper.CasePartyMapper;
import com.hanium.sololaw.domain.cases.repository.CasePartyRepository;
import com.hanium.sololaw.domain.cases.repository.CaseRepository;
import com.hanium.sololaw.domain.cases.repository.LitigationStageRepository;
import com.hanium.sololaw.domain.document.repository.DocumentRepository;
import com.hanium.sololaw.domain.evidence.repository.EvidenceRepository;
import com.hanium.sololaw.domain.schedule.repository.ScheduleRepository;
import com.hanium.sololaw.domain.user.entity.User;
import com.hanium.sololaw.global.common.OffsetPageResponse;
import com.hanium.sololaw.global.exception.CustomException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class CaseServiceImpl implements CaseService {

  private static final List<StageTemplate> STAGE_TEMPLATES =
      List.of(
          new StageTemplate(1, "분쟁 발생"),
          new StageTemplate(2, "내용증명"),
          new StageTemplate(3, "소장 작성"),
          new StageTemplate(4, "법원 접수"),
          new StageTemplate(5, "변론"),
          new StageTemplate(6, "판결"));

  private final CaseRepository caseRepository;
  private final CasePartyRepository casePartyRepository;
  private final LitigationStageRepository litigationStageRepository;
  private final DocumentRepository documentRepository;
  private final EvidenceRepository evidenceRepository;
  private final ScheduleRepository scheduleRepository;
  private final CaseMapper caseMapper;
  private final CasePartyMapper casePartyMapper;

  @Override
  @Transactional
  public CaseResponse createCase(User user, CreateCaseRequest request) {
    log.info(
        "[CaseService] createCase() - START | userId: {}, title: {}",
        user.getId(),
        request.title());

    /*
       1. Case 엔티티 생성 및 저장
    */
    Case caseEntity = caseMapper.toEntity(user.getId(), request);
    Case savedCase = caseRepository.save(caseEntity);

    /*
       2. 당사자 자동 생성
       - 원고(나): is_self=true, 이름은 로그인 사용자 이름
       - 피고: opponentName
    */
    casePartyRepository.save(casePartyMapper.toSelfEntity(savedCase.getId(), user.getName()));
    casePartyRepository.save(
        casePartyMapper.toDefendantEntity(savedCase.getId(), request.opponentName()));

    /*
       3. 절차 6단계 자동 시드
       - caseType이 미정(null)이 아닐 때만 startingStage 기준으로 시드한다.
    */
    if (savedCase.getCaseType() != null) {
      seedLitigationStages(savedCase.getId(), request.startingStage());
    }

    /*
       4. ResponseDto Mapping
    */
    CaseResponse result = caseMapper.toResponse(savedCase);

    log.info("[CaseService] createCase() - END | caseId: {}", savedCase.getId());
    return result;
  }

  @Override
  @Transactional(readOnly = true)
  public OffsetPageResponse<CaseResponse> getCaseList(
      User user, CaseStatus status, CaseType caseType, Pageable pageable) {
    log.info(
        "[CaseService] getCaseList() - START | userId: {}, status: {}, caseType: {}, pageable: {}",
        user.getId(),
        status,
        caseType,
        pageable);

    /*
       1. 필터 조회
       - page/size/sort는 컨트롤러 계층에서 Spring Pageable로 바인딩되어 전달된다(page는 0-base).
    */
    Page<Case> pageResult =
        caseRepository.findAllByUserIdAndFilters(user.getId(), status, caseType, pageable);

    /*
       2. ResponseDto Mapping 및 OffsetPageResponse 래핑
    */
    List<CaseResponse> content = caseMapper.toResponseList(pageResult.getContent());
    OffsetPageResponse<CaseResponse> result =
        OffsetPageResponse.of(
            content,
            pageResult.getTotalElements(),
            pageable.getPageNumber(),
            pageable.getPageSize());

    log.info(
        "[CaseService] getCaseList() - END | totalElements: {}", pageResult.getTotalElements());
    return result;
  }

  @Override
  @Transactional(readOnly = true)
  public CaseDetailResponse getCaseDetail(User user, Long caseId) {
    log.info(
        "[CaseService] getCaseDetail() - START | userId: {}, caseId: {}", user.getId(), caseId);

    /*
       1. 사건 조회 및 소유자 검증
       - 존재하지 않거나 소유자가 아니면 404로 은닉한다.
    */
    Case caseEntity =
        caseRepository
            .findByIdAndUserId(caseId, user.getId())
            .orElseThrow(() -> new CustomException(CaseErrorCode.CASE_NOT_FOUND));

    /*
       2. 당사자 요약 조회
    */
    List<CasePartySummaryResponse> parties =
        casePartyMapper.toSummaryResponseList(casePartyRepository.findAllByCaseId(caseId));

    /*
       3. 문서/증빙/일정 개수 집계
       - 최근활동 집계는 08번 activity_logs 도메인 미구현으로 0 고정값을 사용한다.
       - TODO: 08번 도메인 구현 후 실제 집계 값으로 교체한다.
    */
    int documentCount = (int) documentRepository.countByCaseId(caseId);
    int evidenceCount = (int) evidenceRepository.countByCaseId(caseId);
    int scheduleCount = (int) scheduleRepository.countByCaseId(caseId);

    /*
       4. ResponseDto Mapping
    */
    CaseDetailResponse result =
        caseMapper.toDetailResponse(
            caseEntity, parties, documentCount, evidenceCount, scheduleCount, 0);

    log.info("[CaseService] getCaseDetail() - END | caseId: {}", caseId);
    return result;
  }

  @Override
  @Transactional
  public CaseResponse updateCase(User user, Long caseId, UpdateCaseRequest request) {
    log.info("[CaseService] updateCase() - START | userId: {}, caseId: {}", user.getId(), caseId);

    /*
       1. 사건 조회 및 소유자 검증
    */
    Case caseEntity =
        caseRepository
            .findByIdAndUserId(caseId, user.getId())
            .orElseThrow(() -> new CustomException(CaseErrorCode.CASE_NOT_FOUND));

    /*
       2. caseType 미정→값 전이 여부 확인
       - 전이 시점에만 표준 절차 6단계를 1단계부터 자동 시드한다(원래 startingStage 선택은 복원하지 않음).
    */
    boolean shouldSeedStages = caseEntity.getCaseType() == null && request.caseType() != null;

    /*
       3. 필드 수정
       - null인 필드는 기존 값을 유지한다.
    */
    caseEntity.update(
        request.title() != null ? request.title() : caseEntity.getTitle(),
        request.caseType() != null ? request.caseType() : caseEntity.getCaseType(),
        request.claimAmount() != null ? request.claimAmount() : caseEntity.getClaimAmount(),
        request.court() != null ? request.court() : caseEntity.getCourt(),
        request.caseNumber() != null ? request.caseNumber() : caseEntity.getCaseNumber(),
        request.filingMethod() != null ? request.filingMethod() : caseEntity.getFilingMethod());

    if (shouldSeedStages) {
      seedLitigationStages(caseId, null);
    }

    /*
       4. ResponseDto Mapping
    */
    CaseResponse result = caseMapper.toResponse(caseEntity);

    log.info("[CaseService] updateCase() - END | caseId: {}", caseId);
    return result;
  }

  @Override
  @Transactional
  public CaseResponse updateCaseStatus(User user, Long caseId, UpdateCaseStatusRequest request) {
    log.info(
        "[CaseService] updateCaseStatus() - START | userId: {}, caseId: {}, status: {}",
        user.getId(),
        caseId,
        request.status());

    /*
       1. 사건 조회 및 소유자 검증
    */
    Case caseEntity =
        caseRepository
            .findByIdAndUserId(caseId, user.getId())
            .orElseThrow(() -> new CustomException(CaseErrorCode.CASE_NOT_FOUND));

    /*
       2. 상태 변경
       - reason은 요청 검증(5자 이상)만 거치고 activity_logs(08번) 미구현으로 별도 저장하지 않는다.
       - TODO: 08번 activity_logs 도메인 구현 후 reason을 기록한다.
    */
    caseEntity.updateStatus(request.status());

    /*
       3. ResponseDto Mapping
    */
    CaseResponse result = caseMapper.toResponse(caseEntity);

    log.info("[CaseService] updateCaseStatus() - END | caseId: {}", caseId);
    return result;
  }

  @Override
  @Transactional
  public void deleteCase(User user, Long caseId) {
    log.info("[CaseService] deleteCase() - START | userId: {}, caseId: {}", user.getId(), caseId);

    /*
       1. 사건 조회 및 소유자 검증
    */
    Case caseEntity =
        caseRepository
            .findByIdAndUserId(caseId, user.getId())
            .orElseThrow(() -> new CustomException(CaseErrorCode.CASE_NOT_FOUND));

    /*
       2. 사건 삭제
       - case_parties/litigation_stages/case_todos는 DB CASCADE 제약으로 함께 삭제된다.
    */
    caseRepository.delete(caseEntity);

    log.info("[CaseService] deleteCase() - END | caseId: {}", caseId);
  }

  private void seedLitigationStages(Long caseId, StartingStage startingStage) {
    if (litigationStageRepository.existsByCaseId(caseId)) {
      throw new CustomException(CaseErrorCode.DUPLICATE_STAGE_ORDER);
    }
    int currentOrder = resolveCurrentOrder(startingStage);
    List<LitigationStage> stages =
        STAGE_TEMPLATES.stream()
            .map(
                template ->
                    LitigationStage.builder()
                        .caseId(caseId)
                        .stageOrder(template.order())
                        .name(template.name())
                        .status(resolveSeedStatus(template.order(), currentOrder))
                        .build())
            .toList();
    litigationStageRepository.saveAll(stages);
  }

  private int resolveCurrentOrder(StartingStage startingStage) {
    if (startingStage == null) {
      return 1;
    }
    return switch (startingStage) {
      case DISPUTE -> 1;
      case DEMAND_LETTER -> 2;
      case COMPLAINT_DRAFT -> 3;
      case COURT_FILED -> 5;
    };
  }

  private StageStatus resolveSeedStatus(int order, int currentOrder) {
    if (order < currentOrder) {
      return StageStatus.COMPLETED;
    }
    if (order == currentOrder) {
      return StageStatus.IN_PROGRESS;
    }
    return StageStatus.SCHEDULED;
  }

  private record StageTemplate(int order, String name) {}
}
