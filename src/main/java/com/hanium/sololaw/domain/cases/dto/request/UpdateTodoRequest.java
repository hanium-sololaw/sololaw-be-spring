/* 
 * Copyright (c) HANIUM SOLOLAW 
 */
package com.hanium.sololaw.domain.cases.dto.request;

import java.time.LocalDate;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "할 일 수정/완료 토글 요청 DTO (null인 필드는 변경하지 않음)")
public record UpdateTodoRequest(
    @Schema(description = "할 일 내용") String title,
    @Schema(description = "기한") LocalDate dueDate,
    @Schema(description = "완료 여부") Boolean isDone) {}
