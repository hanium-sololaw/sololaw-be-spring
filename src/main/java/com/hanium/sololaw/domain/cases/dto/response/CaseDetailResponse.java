/* 
 * Copyright (c) HANIUM SOLOLAW 
 */
package com.hanium.sololaw.domain.cases.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import com.hanium.sololaw.domain.cases.entity.enums.CaseStatus;
import com.hanium.sololaw.domain.cases.entity.enums.CaseType;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
@Schema(description = "사건 상세 응답 DTO")
public record CaseDetailResponse(
    @Schema(description = "사건 ID") Long id,
    @Schema(description = "사건번호(접수 전이면 null)") String caseNumber,
    @Schema(description = "사건명") String title,
    @Schema(description = "사건 유형(미정이면 null)") CaseType caseType,
    @Schema(description = "사건 상태") CaseStatus status,
    @Schema(description = "진행률 %(0~100)") Integer progressRate,
    @Schema(description = "관할 법원") String court,
    @Schema(description = "청구금액(원)") BigDecimal claimAmount,
    @Schema(description = "사건 개시 시각") LocalDateTime openedAt,
    @Schema(description = "생성 시각") LocalDateTime createdAt,
    @Schema(description = "수정 시각") LocalDateTime modifiedAt,
    @Schema(description = "당사자 목록") List<CasePartySummaryResponse> parties,
    @Schema(description = "문서 개수(02번 미구현, 현재 0 고정)") Integer documentCount,
    @Schema(description = "증빙자료 개수(03번 미구현, 현재 0 고정)") Integer evidenceCount,
    @Schema(description = "일정 개수(04번 미구현, 현재 0 고정)") Integer scheduleCount,
    @Schema(description = "최근 활동 개수(08번 미구현, 현재 0 고정)") Integer recentActivityCount) {}
