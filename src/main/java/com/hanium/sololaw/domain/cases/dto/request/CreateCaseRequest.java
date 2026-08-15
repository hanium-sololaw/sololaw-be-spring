/* 
 * Copyright (c) HANIUM SOLOLAW 
 */
package com.hanium.sololaw.domain.cases.dto.request;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotBlank;

import com.hanium.sololaw.domain.cases.entity.enums.CaseType;
import com.hanium.sololaw.domain.cases.entity.enums.StartingStage;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "사건 생성 요청 DTO")
public record CreateCaseRequest(
    @NotBlank @Schema(description = "사건명", example = "대여금 반환 청구") String title,
    @Schema(description = "사건 유형(미정이면 null)") CaseType caseType,
    @NotBlank @Schema(description = "상대방(피고) 이름", example = "김철수") String opponentName,
    @Schema(description = "청구금액(원)", example = "5000000") BigDecimal claimAmount,
    @Schema(description = "관할 법원", example = "서울중앙지방법원") String court,
    @Schema(description = "사건번호(접수 전이면 null)", example = "2024가단12345") String caseNumber,
    @Schema(description = "시작 지점(caseType이 미정이 아닐 때만 6단계 시드에 사용, 컬럼 아님)")
        StartingStage startingStage) {}
