/* 
 * Copyright (c) HANIUM SOLOLAW 
 */
package com.hanium.sololaw.domain.cases.dto.response;

import java.time.LocalDate;
import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
@Schema(description = "할 일 응답 DTO")
public record CaseTodoResponse(
    @Schema(description = "할 일 ID") Long id,
    @Schema(description = "할 일 내용") String title,
    @Schema(description = "기한") LocalDate dueDate,
    @Schema(description = "완료 여부") Boolean isDone,
    @Schema(description = "생성 시각") LocalDateTime createdAt,
    @Schema(description = "수정 시각") LocalDateTime modifiedAt) {}
