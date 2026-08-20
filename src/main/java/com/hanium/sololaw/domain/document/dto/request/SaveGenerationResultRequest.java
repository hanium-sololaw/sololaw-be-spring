/* 
 * Copyright (c) HANIUM SOLOLAW 
 */
package com.hanium.sololaw.domain.document.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import com.fasterxml.jackson.databind.JsonNode;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "AI 생성 결과 저장 요청 DTO(RAG done 이벤트를 프론트가 그대로 전달)")
public record SaveGenerationResultRequest(
    @NotBlank @Schema(description = "AI 생성 본문 원문(RAG done.raw_text)") String generatedText,
    @NotNull @Schema(description = "AI 생성 본문 구조화(RAG done.sections)") JsonNode generatedContent) {}
