/* 
 * Copyright (c) HANIUM SOLOLAW 
 */
package com.hanium.sololaw.domain.evidence.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.hanium.sololaw.domain.cases.entity.Case;
import com.hanium.sololaw.domain.cases.exception.CaseErrorCode;
import com.hanium.sololaw.domain.cases.repository.CaseRepository;
import com.hanium.sololaw.domain.evidence.dto.request.CreateEvidenceRequest;
import com.hanium.sololaw.domain.evidence.dto.request.CreateEvidenceUploadUrlRequest;
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
import com.hanium.sololaw.global.exception.CustomException;
import com.hanium.sololaw.global.storage.S3Uploader;

@ExtendWith(MockitoExtension.class)
class EvidenceServiceTest {

  @Mock private CaseRepository caseRepository;
  @Mock private EvidenceRepository evidenceRepository;
  @Mock private SubscriptionRepository subscriptionRepository;
  @Mock private EvidenceMapper evidenceMapper;
  @Mock private S3Uploader s3Uploader;

  @InjectMocks private EvidenceServiceImpl evidenceService;

  @Test
  void createUploadUrl_throwsQuotaExceeded_whenUsedPlusFileSizeExceedsLimit() {
    User user = User.builder().id(1L).build();
    CreateEvidenceUploadUrlRequest request =
        new CreateEvidenceUploadUrlRequest(5L, "big.pdf", "application/pdf", 1_000_000L);
    Case ownedCase = Case.builder().id(5L).userId(1L).build();
    Subscription subscription =
        Subscription.builder()
            .userId(1L)
            .storageLimitBytes(500_000L)
            .usedStorageBytes(499_999L)
            .build();

    when(caseRepository.findByIdAndUserId(5L, 1L)).thenReturn(Optional.of(ownedCase));
    when(subscriptionRepository.findByUserId(1L)).thenReturn(Optional.of(subscription));

    assertThatThrownBy(() -> evidenceService.createUploadUrl(user, request))
        .isInstanceOf(CustomException.class)
        .extracting(e -> ((CustomException) e).getErrorCode())
        .isEqualTo(EvidenceErrorCode.STORAGE_QUOTA_EXCEEDED);
  }

  @Test
  void createUploadUrl_throwsNotFound_whenCaseNotOwned() {
    User user = User.builder().id(1L).build();
    CreateEvidenceUploadUrlRequest request =
        new CreateEvidenceUploadUrlRequest(999L, "file.pdf", "application/pdf", 1000L);
    when(caseRepository.findByIdAndUserId(999L, 1L)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> evidenceService.createUploadUrl(user, request))
        .isInstanceOf(CustomException.class)
        .extracting(e -> ((CustomException) e).getErrorCode())
        .isEqualTo(CaseErrorCode.CASE_NOT_FOUND);
  }

  @Test
  void createUploadUrl_throwsInvalidFileType_whenContentTypeNotAllowed() {
    User user = User.builder().id(1L).build();
    CreateEvidenceUploadUrlRequest request =
        new CreateEvidenceUploadUrlRequest(5L, "malicious.svg", "image/svg+xml", 1000L);
    Case ownedCase = Case.builder().id(5L).userId(1L).build();

    when(caseRepository.findByIdAndUserId(5L, 1L)).thenReturn(Optional.of(ownedCase));

    assertThatThrownBy(() -> evidenceService.createUploadUrl(user, request))
        .isInstanceOf(CustomException.class)
        .extracting(e -> ((CustomException) e).getErrorCode())
        .isEqualTo(EvidenceErrorCode.INVALID_FILE_TYPE);
  }

  @Test
  void createUploadUrl_returnsPresignedUrl_whenWithinQuota() {
    User user = User.builder().id(1L).build();
    CreateEvidenceUploadUrlRequest request =
        new CreateEvidenceUploadUrlRequest(5L, "file.pdf", "application/pdf", 1000L);
    Case ownedCase = Case.builder().id(5L).userId(1L).build();
    Subscription subscription =
        Subscription.builder().userId(1L).storageLimitBytes(500_000L).usedStorageBytes(0L).build();

    when(caseRepository.findByIdAndUserId(5L, 1L)).thenReturn(Optional.of(ownedCase));
    when(subscriptionRepository.findByUserId(1L)).thenReturn(Optional.of(subscription));
    when(s3Uploader.generatePresignedPutUrl(
            anyString(), anyString(), org.mockito.ArgumentMatchers.any()))
        .thenReturn("https://s3.example.com/presigned-put");

    EvidenceUploadUrlResponse result = evidenceService.createUploadUrl(user, request);

    assertThat(result.uploadUrl()).isEqualTo("https://s3.example.com/presigned-put");
    assertThat(result.key()).startsWith("evidence/5/");
  }

  @Test
  void createEvidence_throwsQuotaExceeded_whenReserveFails() {
    User user = User.builder().id(1L).build();
    CreateEvidenceRequest request =
        new CreateEvidenceRequest(
            null,
            ExhibitParty.GAP,
            null,
            "file.pdf",
            "evidence/5/key.pdf",
            1000L,
            "PDF",
            null,
            null,
            null,
            null);
    Case ownedCase = Case.builder().id(5L).userId(1L).build();

    when(caseRepository.findByIdAndUserId(5L, 1L)).thenReturn(Optional.of(ownedCase));
    when(subscriptionRepository.tryReserveStorage(1L, 1000L)).thenReturn(0);

    assertThatThrownBy(() -> evidenceService.createEvidence(user, 5L, request))
        .isInstanceOf(CustomException.class)
        .extracting(e -> ((CustomException) e).getErrorCode())
        .isEqualTo(EvidenceErrorCode.STORAGE_QUOTA_EXCEEDED);
  }

  @Test
  void createEvidence_savesEvidence_whenReserveSucceeds() {
    User user = User.builder().id(1L).build();
    CreateEvidenceRequest request =
        new CreateEvidenceRequest(
            null,
            ExhibitParty.GAP,
            null,
            "file.pdf",
            "evidence/5/key.pdf",
            1000L,
            "PDF",
            null,
            null,
            null,
            null);
    Case ownedCase = Case.builder().id(5L).userId(1L).build();
    Evidence newEvidence = Evidence.builder().caseId(5L).build();
    Evidence savedEvidence = Evidence.builder().id(20L).caseId(5L).build();

    when(caseRepository.findByIdAndUserId(5L, 1L)).thenReturn(Optional.of(ownedCase));
    when(subscriptionRepository.tryReserveStorage(1L, 1000L)).thenReturn(1);
    when(evidenceMapper.toEntity(5L, request)).thenReturn(newEvidence);
    when(evidenceRepository.save(newEvidence)).thenReturn(savedEvidence);
    when(evidenceMapper.toResponse(savedEvidence))
        .thenReturn(EvidenceResponse.builder().id(20L).build());

    EvidenceResponse result = evidenceService.createEvidence(user, 5L, request);

    assertThat(result.id()).isEqualTo(20L);
  }

  @Test
  void getDetail_throwsNotFound_whenNotOwned() {
    User user = User.builder().id(1L).build();
    when(evidenceRepository.findByIdAndUserId(999L, 1L)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> evidenceService.getDetail(user, 999L))
        .isInstanceOf(CustomException.class)
        .extracting(e -> ((CustomException) e).getErrorCode())
        .isEqualTo(EvidenceErrorCode.EVIDENCE_NOT_FOUND);
  }

  @Test
  void updateStatus_setsSubmittedAt_whenTransitioningToSubmitted() {
    User user = User.builder().id(1L).build();
    Evidence evidence =
        Evidence.builder().id(30L).caseId(5L).status(EvidenceStatus.PENDING).build();
    when(evidenceRepository.findByIdAndUserId(30L, 1L)).thenReturn(Optional.of(evidence));
    when(evidenceMapper.toResponse(evidence))
        .thenReturn(EvidenceResponse.builder().id(30L).build());

    evidenceService.updateStatus(
        user,
        30L,
        new com.hanium.sololaw.domain.evidence.dto.request.UpdateEvidenceStatusRequest(
            EvidenceStatus.SUBMITTED));

    assertThat(evidence.getStatus()).isEqualTo(EvidenceStatus.SUBMITTED);
    assertThat(evidence.getSubmittedAt()).isNotNull();
  }

  @Test
  void getDownloadUrl_returnsPresignedUrl() {
    User user = User.builder().id(1L).build();
    Evidence evidence =
        Evidence.builder()
            .id(40L)
            .caseId(5L)
            .fileUrl("evidence/5/key.pdf")
            .fileName("계약서.pdf")
            .build();
    when(evidenceRepository.findByIdAndUserId(40L, 1L)).thenReturn(Optional.of(evidence));
    when(s3Uploader.generatePresignedGetUrl(
            "evidence/5/key.pdf", Duration.ofMinutes(10), "계약서.pdf"))
        .thenReturn("https://s3.example.com/presigned-get");

    String result = evidenceService.getDownloadUrl(user, 40L);

    assertThat(result).isEqualTo("https://s3.example.com/presigned-get");
  }

  @Test
  void delete_releasesStorageAndDeletesS3Object() {
    User user = User.builder().id(1L).build();
    Evidence evidence =
        Evidence.builder().id(50L).caseId(5L).fileUrl("evidence/5/key.pdf").fileSize(1000L).build();
    when(evidenceRepository.findByIdAndUserId(50L, 1L)).thenReturn(Optional.of(evidence));

    evidenceService.delete(user, 50L);

    verify(subscriptionRepository).releaseStorage(1L, 1000L);
    verify(s3Uploader).deleteObject("evidence/5/key.pdf");
    verify(evidenceRepository).delete(evidence);
  }

  @Test
  void getNextExhibitNo_throwsNotFound_whenCaseNotOwned() {
    User user = User.builder().id(1L).build();
    when(caseRepository.findByIdAndUserId(999L, 1L)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> evidenceService.getNextExhibitNo(user, 999L, ExhibitParty.GAP))
        .isInstanceOf(CustomException.class)
        .extracting(e -> ((CustomException) e).getErrorCode())
        .isEqualTo(CaseErrorCode.CASE_NOT_FOUND);
  }

  @Test
  void getNextExhibitNo_returnsCountPlusOne() {
    User user = User.builder().id(1L).build();
    Case ownedCase = Case.builder().id(5L).userId(1L).build();
    when(caseRepository.findByIdAndUserId(5L, 1L)).thenReturn(Optional.of(ownedCase));
    when(evidenceRepository.countByCaseIdAndPartyType(5L, ExhibitParty.GAP)).thenReturn(6L);

    int result = evidenceService.getNextExhibitNo(user, 5L, ExhibitParty.GAP);

    assertThat(result).isEqualTo(7);
  }

  @Test
  void getNextExhibitNo_returnsOne_whenNoEvidenceYet() {
    User user = User.builder().id(1L).build();
    Case ownedCase = Case.builder().id(5L).userId(1L).build();
    when(caseRepository.findByIdAndUserId(5L, 1L)).thenReturn(Optional.of(ownedCase));
    when(evidenceRepository.countByCaseIdAndPartyType(5L, ExhibitParty.EUL)).thenReturn(0L);

    int result = evidenceService.getNextExhibitNo(user, 5L, ExhibitParty.EUL);

    assertThat(result).isEqualTo(1);
  }

  @Test
  void replaceFile_throwsQuotaExceeded_whenReserveFails() {
    User user = User.builder().id(1L).build();
    Evidence previous = Evidence.builder().id(60L).caseId(5L).partyType(ExhibitParty.GAP).build();
    com.hanium.sololaw.domain.evidence.dto.request.ReplaceEvidenceFileRequest request =
        new com.hanium.sololaw.domain.evidence.dto.request.ReplaceEvidenceFileRequest(
            "new.pdf", "evidence/5/new-key.pdf", 2000L, "PDF");

    when(evidenceRepository.findByIdAndUserId(60L, 1L)).thenReturn(Optional.of(previous));
    when(subscriptionRepository.tryReserveStorage(1L, 2000L)).thenReturn(0);

    assertThatThrownBy(() -> evidenceService.replaceFile(user, 60L, request))
        .isInstanceOf(CustomException.class)
        .extracting(e -> ((CustomException) e).getErrorCode())
        .isEqualTo(EvidenceErrorCode.STORAGE_QUOTA_EXCEEDED);
  }

  @Test
  void replaceFile_demotesPreviousAndSavesNewLatestVersion_whenReserveSucceeds() {
    User user = User.builder().id(1L).build();
    Evidence previous =
        Evidence.builder()
            .id(60L)
            .caseId(5L)
            .partyType(ExhibitParty.GAP)
            .exhibitNo("4")
            .proofPurpose("피고가 보증금 반환을 회피한 사실 입증")
            .build();
    com.hanium.sololaw.domain.evidence.dto.request.ReplaceEvidenceFileRequest request =
        new com.hanium.sololaw.domain.evidence.dto.request.ReplaceEvidenceFileRequest(
            "new.pdf", "evidence/5/new-key.pdf", 2000L, "PDF");
    Evidence savedEvidence = Evidence.builder().id(61L).caseId(5L).build();

    when(evidenceRepository.findByIdAndUserId(60L, 1L)).thenReturn(Optional.of(previous));
    when(subscriptionRepository.tryReserveStorage(1L, 2000L)).thenReturn(1);
    when(evidenceRepository.save(org.mockito.ArgumentMatchers.any(Evidence.class)))
        .thenReturn(savedEvidence);
    when(evidenceMapper.toResponse(savedEvidence))
        .thenReturn(EvidenceResponse.builder().id(61L).build());

    EvidenceResponse result = evidenceService.replaceFile(user, 60L, request);

    assertThat(result.id()).isEqualTo(61L);
    assertThat(previous.getIsLatest()).isFalse();
    // tryReserveStorage()가 clearAutomatically=true라 dirty checking만으로는 previous가 저장되지
    // 않으므로(준영속 상태로 전환됨) 명시적 save() 호출을 반드시 거쳐야 한다.
    verify(evidenceRepository).save(previous);
  }
}
