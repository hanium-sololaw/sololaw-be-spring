/* 
 * Copyright (c) HANIUM SOLOLAW 
 */
package com.hanium.sololaw.domain.cases.dto.request;

import java.math.BigDecimal;

import com.hanium.sololaw.domain.cases.entity.enums.CaseType;
import com.hanium.sololaw.domain.cases.entity.enums.FilingMethod;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "사건 수정 요청 DTO (null인 필드는 변경하지 않음)")
public record UpdateCaseRequest(
    @Schema(description = "사건명") String title,
    @Schema(description = "사건 유형(미정→값 지정 시 그 시점에 절차 6단계 자동 시드)") CaseType caseType,
    @Schema(description = "청구금액(원)") BigDecimal claimAmount,
    @Schema(description = "관할 법원") String court,
    @Schema(description = "사건번호") String caseNumber,
    @Schema(description = "법원 접수 방법(전자소송/종이 제출)") FilingMethod filingMethod) {}
