/* 
 * Copyright (c) HANIUM SOLOLAW 
 */
package com.hanium.sololaw.domain.document.dto.response;

import java.time.LocalDateTime;

import com.hanium.sololaw.domain.document.entity.enums.JobStatus;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
@Schema(description = "문서 생성 로그 응답 DTO(선택 기록)")
public record DocumentGenerationJobResponse(
    @Schema(description = "잡 ID") Long id,
    @Schema(description = "대상 문서 ID") Long documentId,
    @Schema(description = "잡 상태") JobStatus status,
    @Schema(description = "진행률 0~100") Integer progress,
    @Schema(description = "실패 코드") String errorCode,
    @Schema(description = "실패 사유") String errorMessage,
    @Schema(description = "실패 시각") LocalDateTime failedAt) {}
