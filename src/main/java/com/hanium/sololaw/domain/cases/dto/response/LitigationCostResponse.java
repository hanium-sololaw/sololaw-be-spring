/* 
 * Copyright (c) HANIUM SOLOLAW 
 */
package com.hanium.sololaw.domain.cases.dto.response;

import com.hanium.sololaw.domain.cases.entity.enums.LitigationInstance;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "소송비용(인지대·송달료) 산출 응답 DTO")
public record LitigationCostResponse(
    @Schema(description = "소가(청구금액)", example = "10000000") long claimAmount,
    @Schema(description = "소액사건 여부(소가 3천만원 이하, 1심 송달 회차 산정에만 사용)", example = "false")
        boolean isSmallClaim,
    @Schema(description = "전자소송 여부(인지액 10% 감경 대상)", example = "true") boolean isElectronicFiling,
    @Schema(description = "심급(1심/항소/상고 — 인지액 배율에 반영)") LitigationInstance instance,
    @Schema(description = "인지액(심급 배율·전자소송 감경 반영 후 금액)", example = "45000") long stampFee,
    @Schema(description = "송달료 총액", example = "169200") long deliveryFee,
    @Schema(description = "인지액 + 송달료", example = "214200") long totalCost,
    @Schema(description = "당사자 수(송달료 계산에 사용)", example = "2") int partyCount,
    @Schema(description = "당사자 1인당 송달 회차(1심 일반 15회·소액 10회, 항소 12회, 상고 8회)", example = "15")
        int deliveryCount,
    @Schema(description = "산출 방식 안내 및 유의사항") String disclaimer) {}
