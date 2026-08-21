/* 
 * Copyright (c) HANIUM SOLOLAW 
 */
package com.hanium.sololaw.domain.evidence.dto.request;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "증거 폴더 수정 요청 DTO (null인 필드는 변경하지 않음)")
public record UpdateEvidenceFolderRequest(
    @Schema(description = "폴더명") String name,
    @Schema(description = "분류") String folderType,
    @Schema(description = "태그 목록") List<String> tags) {}
