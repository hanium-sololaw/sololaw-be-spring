/* 
 * Copyright (c) HANIUM SOLOLAW 
 */
package com.hanium.sololaw.domain.cases.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.hanium.sololaw.domain.cases.entity.enums.CaseStatus;
import com.hanium.sololaw.domain.cases.entity.enums.CaseType;
import com.hanium.sololaw.domain.cases.entity.enums.FilingMethod;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
@Schema(description = "사건 응답 DTO")
public record CaseResponse(
    @Schema(description = "사건 ID") Long id,
    @Schema(description = "사건번호(접수 전이면 null)") String caseNumber,
    @Schema(description = "사건명") String title,
    @Schema(description = "사건 유형(미정이면 null)") CaseType caseType,
    @Schema(description = "사건 상태") CaseStatus status,
    @Schema(description = "진행률 %(0~100)") Integer progressRate,
    @Schema(description = "관할 법원") String court,
    @Schema(description = "청구금액(원)") BigDecimal claimAmount,
    @Schema(description = "사건 개시 시각") LocalDateTime openedAt,
    @Schema(description = "법원 접수 방법(전자소송/종이 제출, 접수 전이면 null)") FilingMethod filingMethod,
    @Schema(description = "생성 시각") LocalDateTime createdAt,
    @Schema(description = "수정 시각") LocalDateTime modifiedAt) {}
