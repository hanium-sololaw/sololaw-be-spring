/* 
 * Copyright (c) HANIUM SOLOLAW 
 */
package com.hanium.sololaw.domain.evidence.service;

import java.time.Duration;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hanium.sololaw.domain.cases.exception.CaseErrorCode;
import com.hanium.sololaw.domain.cases.repository.CaseRepository;
import com.hanium.sololaw.domain.evidence.dto.request.CreateEvidenceRequest;
import com.hanium.sololaw.domain.evidence.dto.request.CreateEvidenceUploadUrlRequest;
import com.hanium.sololaw.domain.evidence.dto.request.ReplaceEvidenceFileRequest;
import com.hanium.sololaw.domain.evidence.dto.request.UpdateEvidenceRequest;
import com.hanium.sololaw.domain.evidence.dto.request.UpdateEvidenceStatusRequest;
import com.hanium.sololaw.domain.evidence.dto.response.EvidenceResponse;
import com.hanium.sololaw.domain.evidence.dto.response.EvidenceUploadUrlResponse;
import com.hanium.sololaw.domain.evidence.entity.Evidence;
import com.hanium.sololaw.domain.evidence.entity.enums.EvidenceStatus;
import com.hanium.sololaw.domain.evidence.entity.enums.ExhibitParty;
import com.hanium.sololaw.domain.evidence.exception.EvidenceErrorCode;
import com.hanium.sololaw.domain.evidence.mapper.EvidenceMapper;
import com.hanium.sololaw.domain.evidence.repository.EvidenceRepository;
import com.hanium.sololaw.domain.subscription.entity.Subscription;
import com.hanium.sololaw.domain.subscription.repository.SubscriptionRepository;
import com.hanium.sololaw.domain.user.entity.User;
import com.hanium.sololaw.global.common.OffsetPageResponse;
import com.hanium.sololaw.global.exception.CustomException;
import com.hanium.sololaw.global.storage.S3Uploader;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class EvidenceServiceImpl implements EvidenceService {

  private static final Duration UPLOAD_URL_EXPIRY = Duration.ofMinutes(10);
  private static final Duration DOWNLOAD_URL_EXPIRY = Duration.ofMinutes(10);

  private final CaseRepository caseRepository;
  private final EvidenceRepository evidenceRepository;
  private final SubscriptionRepository subscriptionRepository;
  private final EvidenceMapper evidenceMapper;
  private final S3Uploader s3Uploader;

  @Override
  @Transactional(readOnly = true)
  public EvidenceUploadUrlResponse createUploadUrl(
      User user, CreateEvidenceUploadUrlRequest request) {
    log.info(
        "[EvidenceService] createUploadUrl() - START | userId: {}, caseId: {}",
        user.getId(),
        request.caseId());

    /*
       1. 사건 소유자 검증
    */
    caseRepository
        .findByIdAndUserId(request.caseId(), user.getId())
        .orElseThrow(() -> new CustomException(CaseErrorCode.CASE_NOT_FOUND));

    /*
       2. 용량 사전 확인
       - 최종 확정은 createEvidence()의 원자 UPDATE에서 이뤄지며, 여기서는 업로드 전 UX용 단순 조회 비교다.
    */
    Subscription subscription =
        subscriptionRepository
            .findByUserId(user.getId())
            .orElseThrow(() -> new CustomException(EvidenceErrorCode.STORAGE_QUOTA_EXCEEDED));
    if (subscription.getUsedStorageBytes() + request.fileSize()
        > subscription.getStorageLimitBytes()) {
      throw new CustomException(EvidenceErrorCode.STORAGE_QUOTA_EXCEEDED);
    }

    /*
       3. presigned PUT URL 발급
    */
    String key =
        "evidence/%d/%s-%s".formatted(request.caseId(), UUID.randomUUID(), request.fileName());
    String uploadUrl =
        s3Uploader.generatePresignedPutUrl(key, request.contentType(), UPLOAD_URL_EXPIRY);
    EvidenceUploadUrlResponse result =
        EvidenceUploadUrlResponse.builder().uploadUrl(uploadUrl).key(key).build();

    log.info("[EvidenceService] createUploadUrl() - END | key: {}", key);
    return result;
  }

  @Override
  @Transactional
  public EvidenceResponse createEvidence(User user, Long caseId, CreateEvidenceRequest request) {
    log.info(
        "[EvidenceService] createEvidence() - START | userId: {}, caseId: {}",
        user.getId(),
        caseId);

    /*
       1. 사건 소유자 검증
    */
    caseRepository
        .findByIdAndUserId(caseId, user.getId())
        .orElseThrow(() -> new CustomException(CaseErrorCode.CASE_NOT_FOUND));

    /*
       2. 저장 용량 원자 예약
       - 영향받은 row가 0이면 용량 초과로 판단해 413을 반환한다.
    */
    int affected = subscriptionRepository.tryReserveStorage(user.getId(), request.fileSize());
    if (affected == 0) {
      throw new CustomException(EvidenceErrorCode.STORAGE_QUOTA_EXCEEDED);
    }

    /*
       3. 증거 저장(status=PENDING)
    */
    Evidence savedEvidence = evidenceRepository.save(evidenceMapper.toEntity(caseId, request));

    /*
       4. ResponseDto Mapping
    */
    EvidenceResponse result = evidenceMapper.toResponse(savedEvidence);

    log.info("[EvidenceService] createEvidence() - END | evidenceId: {}", savedEvidence.getId());
    return result;
  }

  @Override
  @Transactional(readOnly = true)
  public OffsetPageResponse<EvidenceResponse> getList(
      User user,
      Long caseId,
      EvidenceStatus status,
      ExhibitParty partyType,
      Long folderId,
      Boolean isLatest,
      Pageable pageable) {
    log.info(
        "[EvidenceService] getList() - START | userId: {}, caseId: {}, status: {}, partyType: {}, folderId: {}, isLatest: {}",
        user.getId(),
        caseId,
        status,
        partyType,
        folderId,
        isLatest);

    /*
       1. 사건 소유자 검증
       - caseId가 없으면(전체 사건 조회) 건너뛴다.
    */
    if (caseId != null) {
      caseRepository
          .findByIdAndUserId(caseId, user.getId())
          .orElseThrow(() -> new CustomException(CaseErrorCode.CASE_NOT_FOUND));
    }

    /*
       2. 필터 조회
    */
    Page<Evidence> pageResult =
        evidenceRepository.findAllByUserIdAndFilters(
            user.getId(), caseId, status, partyType, folderId, isLatest, pageable);

    /*
       3. ResponseDto Mapping 및 OffsetPageResponse 래핑
    */
    OffsetPageResponse<EvidenceResponse> result =
        OffsetPageResponse.of(
            evidenceMapper.toResponseList(pageResult.getContent()),
            pageResult.getTotalElements(),
            pageable.getPageNumber(),
            pageable.getPageSize());

    log.info(
        "[EvidenceService] getList() - END | totalElements: {}", pageResult.getTotalElements());
    return result;
  }

  @Override
  @Transactional(readOnly = true)
  public EvidenceResponse getDetail(User user, Long evidenceId) {
    log.info(
        "[EvidenceService] getDetail() - START | userId: {}, evidenceId: {}",
        user.getId(),
        evidenceId);

    Evidence evidence = findOwnedEvidence(evidenceId, user.getId());
    EvidenceResponse result = evidenceMapper.toResponse(evidence);

    log.info("[EvidenceService] getDetail() - END | evidenceId: {}", evidenceId);
    return result;
  }

  @Override
  @Transactional
  public EvidenceResponse update(User user, Long evidenceId, UpdateEvidenceRequest request) {
    log.info(
        "[EvidenceService] update() - START | userId: {}, evidenceId: {}",
        user.getId(),
        evidenceId);

    /*
       1. 증거 조회 및 소유자 검증
    */
    Evidence evidence = findOwnedEvidence(evidenceId, user.getId());

    /*
       2. 메타데이터 수정
       - null인 필드는 기존 값을 유지한다(파일 자체 교체는 replaceFile() 사용).
    */
    evidence.update(
        request.exhibitNo() != null ? request.exhibitNo() : evidence.getExhibitNo(),
        request.proofPurpose() != null ? request.proofPurpose() : evidence.getProofPurpose(),
        request.description() != null ? request.description() : evidence.getDescription(),
        request.tags() != null ? evidenceMapper.toArray(request.tags()) : evidence.getTags(),
        request.deadline() != null ? request.deadline() : evidence.getDeadline());

    EvidenceResponse result = evidenceMapper.toResponse(evidence);

    log.info("[EvidenceService] update() - END | evidenceId: {}", evidenceId);
    return result;
  }

  @Override
  @Transactional
  public EvidenceResponse updateStatus(
      User user, Long evidenceId, UpdateEvidenceStatusRequest request) {
    log.info(
        "[EvidenceService] updateStatus() - START | userId: {}, evidenceId: {}, status: {}",
        user.getId(),
        evidenceId,
        request.status());

    Evidence evidence = findOwnedEvidence(evidenceId, user.getId());
    evidence.updateStatus(request.status());
    EvidenceResponse result = evidenceMapper.toResponse(evidence);

    log.info("[EvidenceService] updateStatus() - END | evidenceId: {}", evidenceId);
    return result;
  }

  @Override
  @Transactional(readOnly = true)
  public String getDownloadUrl(User user, Long evidenceId) {
    log.info(
        "[EvidenceService] getDownloadUrl() - START | userId: {}, evidenceId: {}",
        user.getId(),
        evidenceId);

    Evidence evidence = findOwnedEvidence(evidenceId, user.getId());
    String downloadUrl =
        s3Uploader.generatePresignedGetUrl(evidence.getFileUrl(), DOWNLOAD_URL_EXPIRY);

    log.info("[EvidenceService] getDownloadUrl() - END | evidenceId: {}", evidenceId);
    return downloadUrl;
  }

  @Override
  @Transactional
  public void delete(User user, Long evidenceId) {
    log.info(
        "[EvidenceService] delete() - START | userId: {}, evidenceId: {}",
        user.getId(),
        evidenceId);

    /*
       1. 증거 조회 및 소유자 검증
    */
    Evidence evidence = findOwnedEvidence(evidenceId, user.getId());

    /*
       2. 저장 용량 반환 및 S3 실 파일 삭제
    */
    subscriptionRepository.releaseStorage(user.getId(), evidence.getFileSize());
    s3Uploader.deleteObject(evidence.getFileUrl());

    /*
       3. DB 삭제
    */
    evidenceRepository.delete(evidence);

    log.info("[EvidenceService] delete() - END | evidenceId: {}", evidenceId);
  }

  @Override
  @Transactional(readOnly = true)
  public int getNextExhibitNo(User user, Long caseId, ExhibitParty partyType) {
    log.info(
        "[EvidenceService] getNextExhibitNo() - START | userId: {}, caseId: {}, partyType: {}",
        user.getId(),
        caseId,
        partyType);

    /*
       1. 사건 소유자 검증
    */
    caseRepository
        .findByIdAndUserId(caseId, user.getId())
        .orElseThrow(() -> new CustomException(CaseErrorCode.CASE_NOT_FOUND));

    /*
       2. 이미 저장된 증거 건수 + 1
    */
    int nextExhibitNo = (int) evidenceRepository.countByCaseIdAndPartyType(caseId, partyType) + 1;

    log.info("[EvidenceService] getNextExhibitNo() - END | nextExhibitNo: {}", nextExhibitNo);
    return nextExhibitNo;
  }

  @Override
  @Transactional
  public EvidenceResponse replaceFile(
      User user, Long evidenceId, ReplaceEvidenceFileRequest request) {
    log.info(
        "[EvidenceService] replaceFile() - START | userId: {}, evidenceId: {}",
        user.getId(),
        evidenceId);

    /*
       1. 기존 증거 조회 및 소유자 검증
    */
    Evidence previous = findOwnedEvidence(evidenceId, user.getId());

    /*
       2. 저장 용량 원자 예약
       - 새 파일 크기만큼 예약한다. 이전 버전 파일은 이력 보존을 위해 유지하며 용량도 계속 점유한다.
    */
    int affected = subscriptionRepository.tryReserveStorage(user.getId(), request.fileSize());
    if (affected == 0) {
      throw new CustomException(EvidenceErrorCode.STORAGE_QUOTA_EXCEEDED);
    }

    /*
       3. 기존 증거를 이전 버전으로 전환(isLatest=false, 실 파일은 이력 조회를 위해 삭제하지 않음)
       - tryReserveStorage()가 clearAutomatically=true라 previous는 이미 준영속 상태이므로 명시적으로 저장한다.
    */
    previous.updateIsLatest(false);
    evidenceRepository.save(previous);

    /*
       4. exhibitNo·partyType·proofPurpose 등 메타데이터를 물려받은 새 증거 저장(최신본, status는 기본값으로 초기화)
    */
    Evidence newEvidence =
        Evidence.builder()
            .caseId(previous.getCaseId())
            .folderId(previous.getFolderId())
            .partyType(previous.getPartyType())
            .exhibitNo(previous.getExhibitNo())
            .fileName(request.fileName())
            .fileUrl(request.fileUrl())
            .fileSize(request.fileSize())
            .fileType(request.fileType())
            .proofPurpose(previous.getProofPurpose())
            .description(previous.getDescription())
            .tags(previous.getTags())
            .deadline(previous.getDeadline())
            .build();
    Evidence savedEvidence = evidenceRepository.save(newEvidence);

    /*
       5. ResponseDto Mapping
    */
    EvidenceResponse result = evidenceMapper.toResponse(savedEvidence);

    log.info("[EvidenceService] replaceFile() - END | newEvidenceId: {}", savedEvidence.getId());
    return result;
  }

  private Evidence findOwnedEvidence(Long evidenceId, Long userId) {
    return evidenceRepository
        .findByIdAndUserId(evidenceId, userId)
        .orElseThrow(() -> new CustomException(EvidenceErrorCode.EVIDENCE_NOT_FOUND));
  }
}
