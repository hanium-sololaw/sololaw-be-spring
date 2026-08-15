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

@ExtendWith(MockitoExtension.class)
class LitigationStageServiceTest {

  @Mock private CaseRepository caseRepository;
  @Mock private LitigationStageRepository litigationStageRepository;
  @Mock private LitigationStageMapper litigationStageMapper;

  @InjectMocks private LitigationStageServiceImpl litigationStageService;

  @Test
  void updateStageStatus_recalculatesCaseProgressRate() {
    User user = User.builder().id(1L).build();
    Case ownedCase = Case.builder().id(5L).userId(1L).progressRate(0).build();
    LitigationStage stage =
        LitigationStage.builder().id(20L).caseId(5L).stageOrder(3).name("소장 작성").build();
    UpdateStageStatusRequest request = new UpdateStageStatusRequest(StageStatus.COMPLETED);

    when(caseRepository.findByIdAndUserId(5L, 1L)).thenReturn(Optional.of(ownedCase));
    when(litigationStageRepository.findByIdAndCaseId(20L, 5L)).thenReturn(Optional.of(stage));
    when(litigationStageRepository.countByCaseId(5L)).thenReturn(6L);
    when(litigationStageRepository.countByCaseIdAndStatus(5L, StageStatus.COMPLETED))
        .thenReturn(3L);
    when(litigationStageMapper.toResponse(stage))
        .thenReturn(
            LitigationStageResponse.builder().id(20L).status(StageStatus.COMPLETED).build());

    litigationStageService.updateStageStatus(user, 5L, 20L, request);

    assertThat(stage.getStatus()).isEqualTo(StageStatus.COMPLETED);
    assertThat(ownedCase.getProgressRate()).isEqualTo(50);
  }

  @Test
  void updateStageStatus_throwsNotFound_whenStageNotInCase() {
    User user = User.builder().id(1L).build();
    Case ownedCase = Case.builder().id(5L).userId(1L).build();
    UpdateStageStatusRequest request = new UpdateStageStatusRequest(StageStatus.COMPLETED);
    when(caseRepository.findByIdAndUserId(5L, 1L)).thenReturn(Optional.of(ownedCase));
    when(litigationStageRepository.findByIdAndCaseId(999L, 5L)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> litigationStageService.updateStageStatus(user, 5L, 999L, request))
        .isInstanceOf(CustomException.class)
        .extracting(e -> ((CustomException) e).getErrorCode())
        .isEqualTo(CaseErrorCode.LITIGATION_STAGE_NOT_FOUND);
  }
}
