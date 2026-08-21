/* 
 * Copyright (c) HANIUM SOLOLAW 
 */
package com.hanium.sololaw.domain.evidence.dto.response;

import java.time.LocalDateTime;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
@Schema(description = "증거 폴더 응답 DTO")
public record EvidenceFolderResponse(
    @Schema(description = "폴더 ID") Long id,
    @Schema(description = "연결 사건 ID(nullable)") Long caseId,
    @Schema(description = "폴더명") String name,
    @Schema(description = "분류") String folderType,
    @Schema(description = "태그 목록") List<String> tags,
    @Schema(description = "폴더 내 증거 개수") Long evidenceCount,
    @Schema(description = "생성 시각") LocalDateTime createdAt,
    @Schema(description = "수정 시각") LocalDateTime modifiedAt) {}
