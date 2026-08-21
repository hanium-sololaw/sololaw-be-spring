/* 
 * Copyright (c) HANIUM SOLOLAW 
 */
package com.hanium.sololaw.domain.evidence.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "증거 업로드용 presigned URL 발급 요청 DTO")
public record CreateEvidenceUploadUrlRequest(
    @NotNull @Schema(description = "소속 사건 ID") Long caseId,
    @NotBlank @Schema(description = "업로드할 파일명", example = "임대차계약서.pdf") String fileName,
    @NotBlank @Schema(description = "MIME 타입", example = "application/pdf") String contentType,
    @NotNull @Positive @Schema(description = "파일 크기(바이트)") Long fileSize) {}
