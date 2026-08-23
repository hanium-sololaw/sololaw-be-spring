/* 
 * Copyright (c) HANIUM SOLOLAW 
 */
package com.hanium.sololaw.domain.evidence.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "증거 파일 교체 요청 DTO")
public record ReplaceEvidenceFileRequest(
    @NotBlank @Schema(description = "새 파일명", example = "카카오톡_대화내용_v2.pdf") String fileName,
    @NotBlank @Schema(description = "새 파일 key(upload-url 발급 시 받은 key)") String fileUrl,
    @NotNull @Positive @Schema(description = "새 파일 크기(바이트)") Long fileSize,
    @Schema(description = "새 파일 타입(선택)") String fileType) {}
