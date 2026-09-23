/* 
 * Copyright (c) HANIUM SOLOLAW 
 */
package com.hanium.sololaw.domain.courtdocument.client;

/**
 * AI 구조화 출력 원본을 그대로 담는 레코드. 값을 못 찾으면 null. 날짜·시간은 JSON 스키마상 문자열(ISO 형식)로만 내려오므로 실제 파싱(및 success 여부
 * 판단)은 서비스 레이어에서 처리한다.
 *
 * <p>기일통지서(hearingDate/hearingTime)뿐 아니라 보정명령·소장부본송달(답변서 요구) 등 제출기한이 핵심인 문서
 * (submissionDeadline/deadlineType)도 같은 레코드로 표현한다 — 문서 종류마다 관련 필드만 채워지고 나머지는 null.
 */
public record CourtDocumentExtraction(
    String message,
    String documentType,
    String caseNumber,
    String caseName,
    String hearingType,
    String hearingDate,
    String hearingTime,
    String court,
    String division,
    String submissionDeadline,
    String deadlineType) {}
