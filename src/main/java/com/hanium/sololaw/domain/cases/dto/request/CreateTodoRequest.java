/* 
 * Copyright (c) HANIUM SOLOLAW 
 */
package com.hanium.sololaw.domain.cases.dto.request;

import java.time.LocalDate;

import jakarta.validation.constraints.NotBlank;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "할 일 추가 요청 DTO")
public record CreateTodoRequest(
    @NotBlank @Schema(description = "할 일 내용", example = "증거자료 스캔본 준비") String title,
    @Schema(description = "기한(선택)") LocalDate dueDate) {}
