/* 
 * Copyright (c) HANIUM SOLOLAW 
 */
package com.hanium.sololaw.domain.evidence.dto.request;

import java.util.List;

import jakarta.validation.constraints.NotBlank;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "증거 폴더 생성 요청 DTO")
public record CreateEvidenceFolderRequest(
    @NotBlank @Schema(description = "폴더명") String name,
    @Schema(description = "연결 사건 ID(선택, 미지정 폴더 허용)") Long caseId,
    @Schema(description = "분류(계약서 등)") String folderType,
    @Schema(description = "태그 목록") List<String> tags) {}
