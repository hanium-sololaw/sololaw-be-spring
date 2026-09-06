/* 
 * Copyright (c) HANIUM SOLOLAW 
 */
package com.hanium.sololaw.domain.evidence.dto.request;

import java.time.LocalDate;
import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import com.hanium.sololaw.domain.evidence.entity.enums.ExhibitParty;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "증거 등록 요청 DTO(presigned URL로 업로드 완료 후 메타 확정)")
public record CreateEvidenceRequest(
    @Schema(description = "소속 폴더 ID(선택)") Long folderId,
    @NotNull @Schema(description = "호증 당사자") ExhibitParty partyType,
    @Schema(description = "호증 표기(예: 갑 제1호증)") String exhibitNo,
    @NotBlank @Schema(description = "파일명") String fileName,
    @NotBlank @Schema(description = "업로드된 S3 객체 키(upload-url 발급 시 받은 key)") String fileUrl,
    @NotNull @Positive @Schema(description = "파일 크기(바이트)") Long fileSize,
    @Schema(description = "파일 타입(PDF/JPG/PNG/GIF/WEBP/HEIC/DOCX/HWP)") String fileType,
    @Schema(description = "입증취지") String proofPurpose,
    @Schema(description = "설명") String description,
    @Schema(description = "태그 목록") List<String> tags,
    @Schema(description = "제출 기한") LocalDate deadline) {}
