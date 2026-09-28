/* 
 * Copyright (c) HANIUM SOLOLAW 
 */
package com.hanium.sololaw.domain.courtdocument.service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

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

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class CourtDocumentServiceImpl implements CourtDocumentService {

  private static final Set<String> ALLOWED_CONTENT_TYPES =
      Set.of("application/pdf", "image/jpeg", "image/png");

  private static final String SYSTEM_PROMPT =
      "너는 한국 법원이 발송한 문서(기일통지서·소환장, 보정명령, 소장부본송달 겸 답변서 요구 등)를 읽고 정보를 구조화해서 "
          + "추출하는 어시스턴트다. documentType에는 인식한 문서 종류를 원문 표기 그대로 담아라(예: 기일통지서, "
          + "보정명령, 소장부본송달). "
          + "문서가 법원 문서가 아니거나 글자를 읽을 수 없으면 message에 이유를 담고 나머지 필드는 전부 null로 남겨라. "
          + "기일이 명시된 문서(기일통지서 등)는 hearingDate·hearingTime·hearingType을 채우고, 기일이 아니라 "
          + "제출 기한이 핵심인 문서(보정명령·소장부본송달의 답변서 제출기한 등)는 submissionDeadline·deadlineType을 "
          + "채워라 — 해당하지 않는 쪽은 null로 남긴다. 찾을 수 없는 항목도 추측하지 말고 null로 남겨라. "
          + "hearingDate·submissionDeadline은 yyyy-MM-dd, hearingTime은 24시간 HH:mm 형식으로만 반환해라.";

  private static final String USER_PROMPT = "이 법원 문서에서 정보를 추출해줘.";

  /** 표준 JSON Schema — nullable 필드는 {@code type: ["string","null"]}로 표현(Gemini·OpenAI 공통 문법). */
  private static final Map<String, Object> RESPONSE_SCHEMA =
      Map.of(
          "type",
          "object",
          "properties",
          Map.ofEntries(
              Map.entry("message", nullableString(null)),
              Map.entry("documentType", nullableString("기일통지서/보정명령/소장부본송달 등 원문 표기 그대로")),
              Map.entry("caseNumber", nullableString(null)),
              Map.entry("caseName", nullableString(null)),
              Map.entry("hearingType", nullableString("변론기일/변론준비기일 등 원문 표기 그대로")),
              Map.entry("hearingDate", nullableString("yyyy-MM-dd")),
              Map.entry("hearingTime", nullableString("HH:mm, 24시간")),
              Map.entry("court", nullableString(null)),
              Map.entry("division", nullableString("재판부")),
              Map.entry("submissionDeadline", nullableString("yyyy-MM-dd")),
              Map.entry("deadlineType", nullableString("보정서/답변서/준비서면 등 원문 표기 그대로"))),
          "required",
          List.of(
              "message",
              "documentType",
              "caseNumber",
              "caseName",
              "hearingType",
              "hearingDate",
              "hearingTime",
              "court",
              "division",
              "submissionDeadline",
              "deadlineType"),
          "additionalProperties",
          false);

  private final CourtDocumentRateLimiter rateLimiter;
  private final CourtDocumentFileConverter fileConverter;
  private final GeminiVisionClient geminiVisionClient;
  private final CaseRepository caseRepository;

  @Override
  @Transactional(readOnly = true)
  public CourtDocumentAnalysisResponse analyze(User user, MultipartFile file) {
    log.info("[CourtDocumentService] analyze() - START | userId: {}", user.getId());

    /*
       1. 비용 남용 방지 — 파일 처리·AI 호출 전에 가장 싼 체크부터 수행한다.
    */
    rateLimiter.checkAndIncrement(user.getId());

    /*
       2. 파일 형식 검증
    */
    if (!ALLOWED_CONTENT_TYPES.contains(file.getContentType())) {
      throw new CustomException(CourtDocumentErrorCode.INVALID_FILE_TYPE);
    }

    /*
       3. 파일 변환 → Gemini 구조화 추출
    */
    String fileDataUri = fileConverter.toDataUri(file);
    CourtDocumentExtraction extraction =
        geminiVisionClient.extractStructured(
            SYSTEM_PROMPT,
            USER_PROMPT,
            fileDataUri,
            RESPONSE_SCHEMA,
            CourtDocumentExtraction.class,
            CourtDocumentErrorCode.EXTRACTION_FAILED);

    /*
       4. 사건 매칭 — findByCaseNumberAndUserId라 본인 소유 사건만 매칭된다(크로스유저 노출 없음).
    */
    Long matchedCaseId = matchCase(extraction.caseNumber(), user.getId());

    LocalDate hearingDate = parseDate(extraction.hearingDate());
    LocalTime hearingTime = parseTime(extraction.hearingTime());
    LocalDate submissionDeadline = parseDate(extraction.submissionDeadline());
    boolean success =
        extraction.caseNumber() != null || hearingDate != null || submissionDeadline != null;

    CourtDocumentAnalysisResponse result =
        CourtDocumentAnalysisResponse.builder()
            .success(success)
            .message(success ? null : defaultIfBlank(extraction.message(), "문서에서 정보를 추출하지 못했습니다."))
            .documentType(extraction.documentType())
            .caseNumber(extraction.caseNumber())
            .caseName(extraction.caseName())
            .matchedCaseId(matchedCaseId)
            .scheduleType(resolveScheduleType(hearingDate, submissionDeadline))
            .hearingDate(hearingDate)
            .hearingTime(hearingTime)
            .court(extraction.court())
            .division(extraction.division())
            .submissionDeadline(submissionDeadline)
            .deadlineType(extraction.deadlineType())
            .build();

    log.info(
        "[CourtDocumentService] analyze() - END | userId: {}, success: {}, matchedCaseId: {}",
        user.getId(),
        success,
        matchedCaseId);
    return result;
  }

  /** 기일이 있으면 HEARING, 없고 제출기한만 있으면 SUBMISSION_DEADLINE, 둘 다 없으면 null(프론트가 직접 선택). */
  private ScheduleType resolveScheduleType(LocalDate hearingDate, LocalDate submissionDeadline) {
    if (hearingDate != null) {
      return ScheduleType.HEARING;
    }
    if (submissionDeadline != null) {
      return ScheduleType.SUBMISSION_DEADLINE;
    }
    return null;
  }

  private Long matchCase(String caseNumber, Long userId) {
    if (caseNumber == null || caseNumber.isBlank()) {
      return null;
    }
    return caseRepository
        .findByCaseNumberAndUserId(caseNumber, userId)
        .map(Case::getId)
        .orElse(null);
  }

  private LocalDate parseDate(String value) {
    if (value == null || value.isBlank()) {
      return null;
    }
    try {
      return LocalDate.parse(value);
    } catch (DateTimeParseException e) {
      log.warn("[CourtDocumentService] parseDate() - 잘못된 날짜 형식: {}", value);
      return null;
    }
  }

  private LocalTime parseTime(String value) {
    if (value == null || value.isBlank()) {
      return null;
    }
    try {
      return LocalTime.parse(value);
    } catch (DateTimeParseException e) {
      log.warn("[CourtDocumentService] parseTime() - 잘못된 시각 형식: {}", value);
      return null;
    }
  }

  private String defaultIfBlank(String value, String fallback) {
    return (value == null || value.isBlank()) ? fallback : value;
  }

  private static Map<String, Object> nullableString(String description) {
    return description == null
        ? Map.of("type", List.of("string", "null"))
        : Map.of("type", List.of("string", "null"), "description", description);
  }
}
