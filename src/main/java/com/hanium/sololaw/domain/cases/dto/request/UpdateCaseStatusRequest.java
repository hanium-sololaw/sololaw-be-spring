/* 
 * Copyright (c) HANIUM SOLOLAW 
 */
package com.hanium.sololaw.domain.cases.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import com.hanium.sololaw.domain.cases.entity.enums.CaseStatus;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "사건 상태 변경 요청 DTO")
public record UpdateCaseStatusRequest(
    @NotNull @Schema(description = "변경할 상태") CaseStatus status,
    @NotBlank @Size(min = 5) @Schema(description = "정정 사유(5자 이상)", example = "법원 접수 완료로 상태 정정")
        String reason) {}
