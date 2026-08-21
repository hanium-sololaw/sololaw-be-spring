/* 
 * Copyright (c) HANIUM SOLOLAW 
 */
package com.hanium.sololaw.domain.evidence.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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

@ExtendWith(MockitoExtension.class)
class EvidenceFolderServiceTest {

  @Mock private CaseRepository caseRepository;
  @Mock private EvidenceFolderRepository evidenceFolderRepository;
  @Mock private EvidenceRepository evidenceRepository;
  @Mock private EvidenceFolderMapper evidenceFolderMapper;

  @InjectMocks private EvidenceFolderServiceImpl evidenceFolderService;

  @Test
  void create_throwsNotFound_whenCaseNotOwned() {
    User user = User.builder().id(1L).build();
    CreateEvidenceFolderRequest request = new CreateEvidenceFolderRequest("계약서", 5L, "계약서", null);
    when(caseRepository.findByIdAndUserId(5L, 1L)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> evidenceFolderService.create(user, request))
        .isInstanceOf(CustomException.class)
        .extracting(e -> ((CustomException) e).getErrorCode())
        .isEqualTo(CaseErrorCode.CASE_NOT_FOUND);
  }

  @Test
  void create_savesFolder_whenCaseIdAbsent() {
    User user = User.builder().id(1L).build();
    CreateEvidenceFolderRequest request = new CreateEvidenceFolderRequest("미분류", null, null, null);
    EvidenceFolder newFolder = EvidenceFolder.builder().userId(1L).name("미분류").build();
    EvidenceFolder savedFolder = EvidenceFolder.builder().id(10L).userId(1L).name("미분류").build();

    when(evidenceFolderMapper.toEntity(1L, request)).thenReturn(newFolder);
    when(evidenceFolderRepository.save(newFolder)).thenReturn(savedFolder);
    when(evidenceFolderMapper.toResponse(savedFolder, 0L))
        .thenReturn(EvidenceFolderResponse.builder().id(10L).build());

    EvidenceFolderResponse result = evidenceFolderService.create(user, request);

    assertThat(result.id()).isEqualTo(10L);
  }

  @Test
  void getList_aggregatesEvidenceCountPerFolder() {
    User user = User.builder().id(1L).build();
    EvidenceFolder folder = EvidenceFolder.builder().id(10L).userId(1L).build();
    when(evidenceFolderRepository.findAllByUserIdAndFilters(1L, null)).thenReturn(List.of(folder));
    when(evidenceRepository.countByFolderId(10L)).thenReturn(3L);
    when(evidenceFolderMapper.toResponse(folder, 3L))
        .thenReturn(EvidenceFolderResponse.builder().id(10L).evidenceCount(3L).build());

    List<EvidenceFolderResponse> result = evidenceFolderService.getList(user, null);

    assertThat(result).hasSize(1);
    assertThat(result.get(0).evidenceCount()).isEqualTo(3L);
  }

  @Test
  void update_throwsNotFound_whenFolderNotOwned() {
    User user = User.builder().id(1L).build();
    UpdateEvidenceFolderRequest request = new UpdateEvidenceFolderRequest("변경명", null, null);
    when(evidenceFolderRepository.findByIdAndUserId(999L, 1L)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> evidenceFolderService.update(user, 999L, request))
        .isInstanceOf(CustomException.class)
        .extracting(e -> ((CustomException) e).getErrorCode())
        .isEqualTo(EvidenceErrorCode.EVIDENCE_FOLDER_NOT_FOUND);
  }

  @Test
  void delete_removesFolder_withoutTouchingS3() {
    User user = User.builder().id(1L).build();
    EvidenceFolder folder = EvidenceFolder.builder().id(10L).userId(1L).build();
    when(evidenceFolderRepository.findByIdAndUserId(10L, 1L)).thenReturn(Optional.of(folder));

    evidenceFolderService.delete(user, 10L);

    org.mockito.Mockito.verify(evidenceFolderRepository).delete(folder);
    org.mockito.Mockito.verifyNoInteractions(evidenceRepository);
  }
}
