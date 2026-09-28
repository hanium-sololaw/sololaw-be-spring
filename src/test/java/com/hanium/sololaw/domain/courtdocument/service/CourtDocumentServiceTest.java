/* 
 * Copyright (c) HANIUM SOLOLAW 
 */
package com.hanium.sololaw.domain.courtdocument.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import com.hanium.sololaw.domain.cases.entity.Case;
import com.hanium.sololaw.domain.cases.repository.CaseRepository;
import com.hanium.sololaw.domain.courtdocument.CourtDocumentRateLimiter;
import com.hanium.sololaw.domain.courtdocument.client.CourtDocumentExtraction;
import com.hanium.sololaw.domain.courtdocument.converter.CourtDocumentFileConverter;
import com.hanium.sololaw.domain.courtdocument.dto.response.CourtDocumentAnalysisResponse;
import com.hanium.sololaw.domain.courtdocument.exception.CourtDocumentErrorCode;
import com.hanium.sololaw.domain.schedule.entity.enums.ScheduleType;
import com.hanium.sololaw.domain.user.entity.User;
import com.hanium.sololaw.global.ai.GeminiVisionClient;
import com.hanium.sololaw.global.exception.CustomException;

@ExtendWith(MockitoExtension.class)
class CourtDocumentServiceTest {

  @Mock private CourtDocumentRateLimiter rateLimiter;
  @Mock private CourtDocumentFileConverter fileConverter;
  @Mock private GeminiVisionClient geminiVisionClient;
  @Mock private CaseRepository caseRepository;

  @InjectMocks private CourtDocumentServiceImpl courtDocumentService;

  @Test
  void analyze_throwsRateLimited_whenRateLimiterRejects() {
    User user = User.builder().id(1L).build();
    MockMultipartFile file =
        new MockMultipartFile("file", "notice.pdf", "application/pdf", new byte[] {1, 2, 3});

    doThrow(new CustomException(CourtDocumentErrorCode.RATE_LIMITED))
        .when(rateLimiter)
        .checkAndIncrement(1L);

    assertThatThrownBy(() -> courtDocumentService.analyze(user, file))
        .isInstanceOf(CustomException.class)
        .extracting(e -> ((CustomException) e).getErrorCode())
        .isEqualTo(CourtDocumentErrorCode.RATE_LIMITED);

    verify(geminiVisionClient, never())
        .extractStructured(any(), any(), any(), any(), eq(CourtDocumentExtraction.class), any());
  }

  @Test
  void analyze_throwsInvalidFileType_whenContentTypeNotAllowed() {
    User user = User.builder().id(1L).build();
    MockMultipartFile file =
        new MockMultipartFile("file", "notice.txt", "text/plain", new byte[] {1, 2, 3});

    assertThatThrownBy(() -> courtDocumentService.analyze(user, file))
        .isInstanceOf(CustomException.class)
        .extracting(e -> ((CustomException) e).getErrorCode())
        .isEqualTo(CourtDocumentErrorCode.INVALID_FILE_TYPE);

    verify(geminiVisionClient, never())
        .extractStructured(any(), any(), any(), any(), eq(CourtDocumentExtraction.class), any());
  }

  @Test
  void analyze_returnsHearingSchedule_whenDocumentIsHearingNotice() {
    User user = User.builder().id(1L).build();
    MockMultipartFile file =
        new MockMultipartFile("file", "notice.jpg", "image/jpeg", new byte[] {1, 2, 3});
    Case ownedCase = Case.builder().id(10L).userId(1L).caseNumber("2026가단123456").build();
    CourtDocumentExtraction extraction =
        new CourtDocumentExtraction(
            null,
            "기일통지서",
            "2026가단123456",
            "임대차 보증금 반환 청구",
            "변론기일",
            "2026-05-31",
            "10:00",
            "서울중앙지방법원",
            "제51단독",
            null,
            null);

    when(fileConverter.toDataUri(file)).thenReturn("data:image/jpeg;base64,xxx");
    when(geminiVisionClient.extractStructured(
            any(),
            any(),
            eq("data:image/jpeg;base64,xxx"),
            any(),
            eq(CourtDocumentExtraction.class),
            any()))
        .thenReturn(extraction);
    when(caseRepository.findByCaseNumberAndUserId("2026가단123456", 1L))
        .thenReturn(Optional.of(ownedCase));

    CourtDocumentAnalysisResponse result = courtDocumentService.analyze(user, file);

    assertThat(result.success()).isTrue();
    assertThat(result.documentType()).isEqualTo("기일통지서");
    assertThat(result.matchedCaseId()).isEqualTo(10L);
    assertThat(result.scheduleType()).isEqualTo(ScheduleType.HEARING);
    assertThat(result.hearingDate()).isEqualTo(LocalDate.of(2026, 5, 31));
  }

  @Test
  void analyze_returnsSubmissionDeadlineSchedule_whenDocumentIsCorrectionOrder() {
    User user = User.builder().id(1L).build();
    MockMultipartFile file =
        new MockMultipartFile("file", "notice.pdf", "application/pdf", new byte[] {1, 2, 3});
    CourtDocumentExtraction extraction =
        new CourtDocumentExtraction(
            null,
            "보정명령",
            "2026가단123456",
            "임대차 보증금 반환 청구",
            null,
            null,
            null,
            "서울중앙지방법원",
            "제51단독",
            "2026-06-15",
            "보정서");

    when(fileConverter.toDataUri(file)).thenReturn("data:application/pdf;base64,xxx");
    when(geminiVisionClient.extractStructured(
            any(),
            any(),
            eq("data:application/pdf;base64,xxx"),
            any(),
            eq(CourtDocumentExtraction.class),
            any()))
        .thenReturn(extraction);
    when(caseRepository.findByCaseNumberAndUserId("2026가단123456", 1L)).thenReturn(Optional.empty());

    CourtDocumentAnalysisResponse result = courtDocumentService.analyze(user, file);

    assertThat(result.success()).isTrue();
    assertThat(result.documentType()).isEqualTo("보정명령");
    assertThat(result.scheduleType()).isEqualTo(ScheduleType.SUBMISSION_DEADLINE);
    assertThat(result.submissionDeadline()).isEqualTo(LocalDate.of(2026, 6, 15));
    assertThat(result.deadlineType()).isEqualTo("보정서");
    assertThat(result.hearingDate()).isNull();
    assertThat(result.matchedCaseId()).isNull();
  }

  @Test
  void analyze_returnsFailure_whenExtractionEmpty() {
    User user = User.builder().id(1L).build();
    MockMultipartFile file =
        new MockMultipartFile("file", "notice.jpg", "image/jpeg", new byte[] {1, 2, 3});
    CourtDocumentExtraction extraction =
        new CourtDocumentExtraction(
            "법원 문서로 보이지 않습니다.", null, null, null, null, null, null, null, null, null, null);

    when(fileConverter.toDataUri(file)).thenReturn("data:image/jpeg;base64,xxx");
    when(geminiVisionClient.extractStructured(
            any(),
            any(),
            eq("data:image/jpeg;base64,xxx"),
            any(),
            eq(CourtDocumentExtraction.class),
            any()))
        .thenReturn(extraction);

    CourtDocumentAnalysisResponse result = courtDocumentService.analyze(user, file);

    assertThat(result.success()).isFalse();
    assertThat(result.message()).isEqualTo("법원 문서로 보이지 않습니다.");
    assertThat(result.matchedCaseId()).isNull();
  }

  @Test
  void analyze_propagatesExtractionFailed_whenGeminiClientThrows() {
    User user = User.builder().id(1L).build();
    MockMultipartFile file =
        new MockMultipartFile("file", "notice.jpg", "image/jpeg", new byte[] {1, 2, 3});

    when(fileConverter.toDataUri(file)).thenReturn("data:image/jpeg;base64,xxx");
    when(geminiVisionClient.extractStructured(
            any(),
            any(),
            eq("data:image/jpeg;base64,xxx"),
            any(),
            eq(CourtDocumentExtraction.class),
            any()))
        .thenThrow(new CustomException(CourtDocumentErrorCode.EXTRACTION_FAILED));

    assertThatThrownBy(() -> courtDocumentService.analyze(user, file))
        .isInstanceOf(CustomException.class)
        .extracting(e -> ((CustomException) e).getErrorCode())
        .isEqualTo(CourtDocumentErrorCode.EXTRACTION_FAILED);
  }
}
