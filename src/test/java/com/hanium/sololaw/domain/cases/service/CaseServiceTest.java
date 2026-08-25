/* 
 * Copyright (c) HANIUM SOLOLAW 
 */
package com.hanium.sololaw.domain.cases.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.hanium.sololaw.domain.cases.dto.request.CreateCaseRequest;
import com.hanium.sololaw.domain.cases.dto.request.UpdateCaseRequest;
import com.hanium.sololaw.domain.cases.dto.request.UpdateCaseStatusRequest;
import com.hanium.sololaw.domain.cases.dto.response.ActivityLogResponse;
import com.hanium.sololaw.domain.cases.dto.response.CaseDetailResponse;
import com.hanium.sololaw.domain.cases.dto.response.CaseResponse;
import com.hanium.sololaw.domain.cases.entity.ActivityLog;
import com.hanium.sololaw.domain.cases.entity.Case;
import com.hanium.sololaw.domain.cases.entity.CaseParty;
import com.hanium.sololaw.domain.cases.entity.LitigationStage;
import com.hanium.sololaw.domain.cases.entity.enums.CaseStatus;
import com.hanium.sololaw.domain.cases.entity.enums.CaseType;
import com.hanium.sololaw.domain.cases.entity.enums.StageStatus;
import com.hanium.sololaw.domain.cases.entity.enums.StartingStage;
import com.hanium.sololaw.domain.cases.exception.CaseErrorCode;
import com.hanium.sololaw.domain.cases.mapper.CaseMapper;
import com.hanium.sololaw.domain.cases.mapper.CasePartyMapper;
import com.hanium.sololaw.domain.cases.repository.ActivityLogRepository;
import com.hanium.sololaw.domain.cases.repository.CasePartyRepository;
import com.hanium.sololaw.domain.cases.repository.CaseRepository;
import com.hanium.sololaw.domain.cases.repository.LitigationStageRepository;
import com.hanium.sololaw.domain.document.repository.DocumentRepository;
import com.hanium.sololaw.domain.evidence.repository.EvidenceRepository;
import com.hanium.sololaw.domain.schedule.repository.ScheduleRepository;
import com.hanium.sololaw.domain.user.entity.User;
import com.hanium.sololaw.global.exception.CustomException;

@ExtendWith(MockitoExtension.class)
class CaseServiceTest {

  @Mock private CaseRepository caseRepository;
  @Mock private CasePartyRepository casePartyRepository;
  @Mock private LitigationStageRepository litigationStageRepository;
  @Mock private DocumentRepository documentRepository;
  @Mock private EvidenceRepository evidenceRepository;
  @Mock private ScheduleRepository scheduleRepository;
  @Mock private ActivityLogRepository activityLogRepository;
  @Mock private CaseMapper caseMapper;
  @Mock private CasePartyMapper casePartyMapper;

  @InjectMocks private CaseServiceImpl caseService;

  @Test
  @SuppressWarnings("unchecked")
  void createCase_seedsSixStagesFromStartingStage_whenCaseTypeProvided() {
    User user = User.builder().id(1L).name("김철수").build();
    CreateCaseRequest request =
        new CreateCaseRequest(
            "대여금 반환 청구", CaseType.LOAN, "이영희", null, null, null, StartingStage.DEMAND_LETTER);
    Case newCase = Case.builder().userId(1L).title(request.title()).caseType(CaseType.LOAN).build();
    Case savedCase =
        Case.builder().id(100L).userId(1L).title(request.title()).caseType(CaseType.LOAN).build();

    when(caseMapper.toEntity(1L, request)).thenReturn(newCase);
    when(caseRepository.save(newCase)).thenReturn(savedCase);
    when(casePartyMapper.toSelfEntity(100L, "김철수")).thenReturn(CaseParty.builder().build());
    when(casePartyMapper.toDefendantEntity(100L, "이영희")).thenReturn(CaseParty.builder().build());
    when(caseMapper.toResponse(savedCase)).thenReturn(CaseResponse.builder().id(100L).build());

    caseService.createCase(user, request);

    ArgumentCaptor<List<LitigationStage>> captor = ArgumentCaptor.forClass(List.class);
    verify(litigationStageRepository).saveAll(captor.capture());
    List<LitigationStage> stages = captor.getValue();
    assertThat(stages).hasSize(6);
    // DEMAND_LETTER -> currentOrder=2
    assertThat(stages.get(0).getStatus()).isEqualTo(StageStatus.COMPLETED);
    assertThat(stages.get(1).getStatus()).isEqualTo(StageStatus.IN_PROGRESS);
    assertThat(stages.get(2).getStatus()).isEqualTo(StageStatus.SCHEDULED);
  }

  @Test
  void createCase_doesNotSeedStages_whenCaseTypeIsNull() {
    User user = User.builder().id(1L).name("김철수").build();
    CreateCaseRequest request =
        new CreateCaseRequest("아직 모르겠어요", null, "이영희", null, null, null, null);
    Case newCase = Case.builder().userId(1L).title(request.title()).build();
    Case savedCase = Case.builder().id(100L).userId(1L).title(request.title()).build();

    when(caseMapper.toEntity(1L, request)).thenReturn(newCase);
    when(caseRepository.save(newCase)).thenReturn(savedCase);
    when(casePartyMapper.toSelfEntity(100L, "김철수")).thenReturn(CaseParty.builder().build());
    when(casePartyMapper.toDefendantEntity(100L, "이영희")).thenReturn(CaseParty.builder().build());
    when(caseMapper.toResponse(savedCase)).thenReturn(CaseResponse.builder().id(100L).build());

    caseService.createCase(user, request);

    verify(litigationStageRepository, never()).saveAll(any());
  }

  @Test
  void getCaseDetail_throwsNotFound_whenNotOwned() {
    User user = User.builder().id(1L).build();
    when(caseRepository.findByIdAndUserId(999L, 1L)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> caseService.getCaseDetail(user, 999L))
        .isInstanceOf(CustomException.class)
        .extracting(e -> ((CustomException) e).getErrorCode())
        .isEqualTo(CaseErrorCode.CASE_NOT_FOUND);
  }

  @Test
  @SuppressWarnings("unchecked")
  void getCaseDetail_aggregatesRecentActivity() {
    User user = User.builder().id(1L).build();
    Case existingCase = Case.builder().id(5L).userId(1L).build();
    ActivityLog activityLog =
        ActivityLog.builder().caseId(5L).description("사건 상태가 작성 중에서 진행 중으로 변경됨: 접수 완료").build();

    when(caseRepository.findByIdAndUserId(5L, 1L)).thenReturn(Optional.of(existingCase));
    when(casePartyRepository.findAllByCaseId(5L)).thenReturn(List.of());
    when(casePartyMapper.toSummaryResponseList(List.of())).thenReturn(List.of());
    when(documentRepository.countByCaseId(5L)).thenReturn(2L);
    when(evidenceRepository.countByCaseId(5L)).thenReturn(3L);
    when(scheduleRepository.countByCaseId(5L)).thenReturn(1L);
    when(activityLogRepository.countByCaseId(5L)).thenReturn(1L);
    when(activityLogRepository.findTop5ByCaseIdOrderByCreatedAtDesc(5L))
        .thenReturn(List.of(activityLog));
    when(caseMapper.toDetailResponse(any(), any(), anyInt(), anyInt(), anyInt(), anyInt(), any()))
        .thenReturn(CaseDetailResponse.builder().id(5L).build());

    caseService.getCaseDetail(user, 5L);

    ArgumentCaptor<List<ActivityLogResponse>> activitiesCaptor =
        ArgumentCaptor.forClass(List.class);
    verify(caseMapper)
        .toDetailResponse(
            eq(existingCase), any(), eq(2), eq(3), eq(1), eq(1), activitiesCaptor.capture());
    assertThat(activitiesCaptor.getValue()).hasSize(1);
    assertThat(activitiesCaptor.getValue().get(0).description())
        .isEqualTo("사건 상태가 작성 중에서 진행 중으로 변경됨: 접수 완료");
  }

  @Test
  void updateCaseStatus_recordsActivityLog_withPreviousAndNewStatus() {
    User user = User.builder().id(1L).build();
    Case existingCase = Case.builder().id(5L).userId(1L).status(CaseStatus.PREPARING).build();
    UpdateCaseStatusRequest request =
        new UpdateCaseStatusRequest(CaseStatus.IN_PROGRESS, "법원 접수 완료로 상태 정정");
    when(caseRepository.findByIdAndUserId(5L, 1L)).thenReturn(Optional.of(existingCase));
    when(caseMapper.toResponse(existingCase)).thenReturn(CaseResponse.builder().id(5L).build());

    caseService.updateCaseStatus(user, 5L, request);

    ArgumentCaptor<ActivityLog> captor = ArgumentCaptor.forClass(ActivityLog.class);
    verify(activityLogRepository).save(captor.capture());
    assertThat(captor.getValue().getCaseId()).isEqualTo(5L);
    assertThat(captor.getValue().getDescription())
        .contains("작성 중")
        .contains("진행 중")
        .contains("법원 접수 완료로 상태 정정");
  }

  @Test
  @SuppressWarnings("unchecked")
  void updateCase_seedsStagesFromOrderOne_whenCaseTypeTransitionsFromNullToValue() {
    User user = User.builder().id(1L).build();
    Case existingCase = Case.builder().id(5L).userId(1L).caseType(null).title("old").build();
    UpdateCaseRequest request = new UpdateCaseRequest(null, CaseType.WAGE, null, null, null, null);
    when(caseRepository.findByIdAndUserId(5L, 1L)).thenReturn(Optional.of(existingCase));
    when(caseMapper.toResponse(existingCase)).thenReturn(CaseResponse.builder().id(5L).build());

    caseService.updateCase(user, 5L, request);

    ArgumentCaptor<List<LitigationStage>> captor = ArgumentCaptor.forClass(List.class);
    verify(litigationStageRepository).saveAll(captor.capture());
    assertThat(captor.getValue()).hasSize(6);
    assertThat(captor.getValue().get(0).getStatus()).isEqualTo(StageStatus.IN_PROGRESS);
  }

  @Test
  void updateCase_doesNotReseed_whenCaseTypeAlreadySet() {
    User user = User.builder().id(1L).build();
    Case existingCase =
        Case.builder().id(5L).userId(1L).caseType(CaseType.WAGE).title("old").build();
    UpdateCaseRequest request = new UpdateCaseRequest(null, CaseType.TORT, null, null, null, null);
    when(caseRepository.findByIdAndUserId(5L, 1L)).thenReturn(Optional.of(existingCase));
    when(caseMapper.toResponse(existingCase)).thenReturn(CaseResponse.builder().id(5L).build());

    caseService.updateCase(user, 5L, request);

    verify(litigationStageRepository, never()).saveAll(any());
  }

  @Test
  void deleteCase_throwsNotFound_whenNotOwned() {
    User user = User.builder().id(1L).build();
    when(caseRepository.findByIdAndUserId(999L, 1L)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> caseService.deleteCase(user, 999L))
        .isInstanceOf(CustomException.class)
        .extracting(e -> ((CustomException) e).getErrorCode())
        .isEqualTo(CaseErrorCode.CASE_NOT_FOUND);
  }
}
