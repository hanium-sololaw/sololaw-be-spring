/* 
 * Copyright (c) HANIUM SOLOLAW 
 */
package com.hanium.sololaw.domain.evidence.dto.request;

import java.time.LocalDate;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "증거 수정 요청 DTO, null인 필드는 변경하지 않으며 파일 자체 교체는 별도 API 사용")
public record UpdateEvidenceRequest(
    @Schema(description = "호증 표기") String exhibitNo,
    @Schema(description = "입증취지") String proofPurpose,
    @Schema(description = "설명") String description,
    @Schema(description = "태그 목록") List<String> tags,
    @Schema(description = "제출 기한") LocalDate deadline) {}
