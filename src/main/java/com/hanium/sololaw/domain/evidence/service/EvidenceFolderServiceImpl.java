/* 
 * Copyright (c) HANIUM SOLOLAW 
 */
package com.hanium.sololaw.domain.evidence.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hanium.sololaw.domain.cases.exception.CaseErrorCode;
import com.hanium.sololaw.domain.cases.repository.CaseRepository;
import com.hanium.sololaw.domain.evidence.dto.request.CreateEvidenceFolderRequest;
import com.hanium.sololaw.domain.evidence.dto.request.UpdateEvidenceFolderRequest;
import com.hanium.sololaw.domain.evidence.dto.response.EvidenceFolderResponse;
import com.hanium.sololaw.domain.evidence.entity.EvidenceFolder;
import com.hanium.sololaw.domain.evidence.exception.EvidenceErrorCode;
import com.hanium.sololaw.domain.evidence.mapper.EvidenceFolderMapper;
import com.hanium.sololaw.domain.evidence.repository.EvidenceFolderRepository;
import com.hanium.sololaw.domain.evidence.repository.EvidenceRepository;
import com.hanium.sololaw.domain.user.entity.User;
import com.hanium.sololaw.global.exception.CustomException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class EvidenceFolderServiceImpl implements EvidenceFolderService {

  private final CaseRepository caseRepository;
  private final EvidenceFolderRepository evidenceFolderRepository;
  private final EvidenceRepository evidenceRepository;
  private final EvidenceFolderMapper evidenceFolderMapper;

  @Override
  @Transactional
  public EvidenceFolderResponse create(User user, CreateEvidenceFolderRequest request) {
    log.info(
        "[EvidenceFolderService] create() - START | userId: {}, caseId: {}",
        user.getId(),
        request.caseId());

    /*
       1. caseId가 있으면 사건 소유자 검증
    */
    if (request.caseId() != null) {
      caseRepository
          .findByIdAndUserId(request.caseId(), user.getId())
          .orElseThrow(() -> new CustomException(CaseErrorCode.CASE_NOT_FOUND));
    }

    /*
       2. 폴더 생성 및 저장
    */
    EvidenceFolder savedFolder =
        evidenceFolderRepository.save(evidenceFolderMapper.toEntity(user.getId(), request));

    /*
       3. ResponseDto Mapping
       - 신규 생성 폴더는 증거 개수 0
    */
    EvidenceFolderResponse result = evidenceFolderMapper.toResponse(savedFolder, 0L);

    log.info("[EvidenceFolderService] create() - END | folderId: {}", savedFolder.getId());
    return result;
  }

  @Override
  @Transactional(readOnly = true)
  public List<EvidenceFolderResponse> getList(User user, Long caseId) {
    log.info(
        "[EvidenceFolderService] getList() - START | userId: {}, caseId: {}", user.getId(), caseId);

    /*
       1. 폴더 목록 조회
    */
    List<EvidenceFolder> folders =
        evidenceFolderRepository.findAllByUserIdAndFilters(user.getId(), caseId);

    /*
       2. 폴더별 증거 개수 집계 및 ResponseDto Mapping
    */
    List<EvidenceFolderResponse> result =
        folders.stream()
            .map(
                folder ->
                    evidenceFolderMapper.toResponse(
                        folder, evidenceRepository.countByFolderId(folder.getId())))
            .toList();

    log.info("[EvidenceFolderService] getList() - END | count: {}", result.size());
    return result;
  }

  @Override
  @Transactional
  public EvidenceFolderResponse update(
      User user, Long folderId, UpdateEvidenceFolderRequest request) {
    log.info(
        "[EvidenceFolderService] update() - START | userId: {}, folderId: {}",
        user.getId(),
        folderId);

    /*
       1. 폴더 조회 및 소유자 검증
    */
    EvidenceFolder folder = findOwnedFolder(folderId, user.getId());

    /*
       2. 폴더 수정
       - null인 필드는 기존 값을 유지한다.
    */
    folder.update(
        request.name() != null ? request.name() : folder.getName(),
        request.folderType() != null ? request.folderType() : folder.getFolderType(),
        request.tags() != null ? evidenceFolderMapper.toArray(request.tags()) : folder.getTags());

    /*
       3. ResponseDto Mapping
    */
    EvidenceFolderResponse result =
        evidenceFolderMapper.toResponse(folder, evidenceRepository.countByFolderId(folderId));

    log.info("[EvidenceFolderService] update() - END | folderId: {}", folderId);
    return result;
  }

  @Override
  @Transactional
  public void delete(User user, Long folderId) {
    log.info(
        "[EvidenceFolderService] delete() - START | userId: {}, folderId: {}",
        user.getId(),
        folderId);

    /*
       1. 폴더 조회 및 소유자 검증
    */
    EvidenceFolder folder = findOwnedFolder(folderId, user.getId());

    /*
       2. 폴더 삭제
       - evidence.folder_id는 DB SET NULL 제약으로 보존되며, S3 파일은 삭제하지 않는다.
    */
    evidenceFolderRepository.delete(folder);

    log.info("[EvidenceFolderService] delete() - END | folderId: {}", folderId);
  }

  private EvidenceFolder findOwnedFolder(Long folderId, Long userId) {
    return evidenceFolderRepository
        .findByIdAndUserId(folderId, userId)
        .orElseThrow(() -> new CustomException(EvidenceErrorCode.EVIDENCE_FOLDER_NOT_FOUND));
  }
}
