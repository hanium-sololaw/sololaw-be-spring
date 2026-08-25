/* 
 * Copyright (c) HANIUM SOLOLAW 
 */
package com.hanium.sololaw.domain.cases.dto.response;

import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "사건 최근 활동 응답 DTO")
public record ActivityLogResponse(
    @Schema(description = "활동 설명", example = "사건 상태가 진행 중에서 종결로 변경됨: 판결 확정") String description,
    @Schema(description = "발생 시각") LocalDateTime createdAt) {}
