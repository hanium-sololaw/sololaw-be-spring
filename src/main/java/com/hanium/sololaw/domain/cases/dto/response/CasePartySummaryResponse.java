/* 
 * Copyright (c) HANIUM SOLOLAW 
 */
package com.hanium.sololaw.domain.cases.dto.response;

import com.hanium.sololaw.domain.cases.entity.enums.PartyRole;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
@Schema(description = "당사자 요약 DTO(사건 상세 응답에 포함)")
public record CasePartySummaryResponse(
    @Schema(description = "당사자 ID") Long id,
    @Schema(description = "당사자 역할") PartyRole partyRole,
    @Schema(description = "이름/상호") String name,
    @Schema(description = "원고(나) 여부") Boolean isSelf) {}
