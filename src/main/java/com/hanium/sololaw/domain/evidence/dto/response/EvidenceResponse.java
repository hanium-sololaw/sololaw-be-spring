/* 
 * Copyright (c) HANIUM SOLOLAW 
 */
package com.hanium.sololaw.domain.evidence.dto.response;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import com.hanium.sololaw.domain.evidence.entity.enums.EvidenceStatus;
import com.hanium.sololaw.domain.evidence.entity.enums.ExhibitParty;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
@Schema(description = "증거 응답 DTO")
public record EvidenceResponse(
    @Schema(description = "증거 ID") Long id,
    @Schema(description = "소속 사건 ID") Long caseId,
    @Schema(description = "소속 폴더 ID(nullable)") Long folderId,
    @Schema(description = "호증 당사자") ExhibitParty partyType,
    @Schema(description = "호증 표기") String exhibitNo,
    @Schema(description = "파일명") String fileName,
    @Schema(description = "파일 크기(바이트)") Long fileSize,
    @Schema(description = "파일 타입") String fileType,
    @Schema(description = "제출 상태") EvidenceStatus status,
    @Schema(description = "최신본 여부, false면 교체되어 이전 버전으로 보존된 이력") Boolean isLatest,
    @Schema(description = "입증취지") String proofPurpose,
    @Schema(description = "설명") String description,
    @Schema(description = "태그 목록") List<String> tags,
    @Schema(description = "제출 시각") LocalDateTime submittedAt,
    @Schema(description = "제출 기한") LocalDate deadline,
    @Schema(description = "업로드 시각") LocalDateTime uploadedAt,
    @Schema(description = "생성 시각") LocalDateTime createdAt,
    @Schema(description = "수정 시각") LocalDateTime modifiedAt) {}
