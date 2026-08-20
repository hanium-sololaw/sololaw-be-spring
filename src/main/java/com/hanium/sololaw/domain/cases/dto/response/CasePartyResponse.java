/* 
 * Copyright (c) HANIUM SOLOLAW 
 */
package com.hanium.sololaw.domain.cases.dto.response;

import java.time.LocalDateTime;

import com.hanium.sololaw.domain.cases.entity.enums.PartyRole;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
@Schema(description = "당사자 응답 DTO")
public record CasePartyResponse(
    @Schema(description = "당사자 ID") Long id,
    @Schema(description = "당사자 역할") PartyRole partyRole,
    @Schema(description = "이름/상호") String name,
    @Schema(description = "주민등록번호(마스킹)", example = "990101-1******") String residentNoMasked,
    @Schema(description = "주소") String address,
    @Schema(description = "연락처") String phone,
    @Schema(description = "원고(나) 여부") Boolean isSelf,
    @Schema(description = "생성 시각") LocalDateTime createdAt,
    @Schema(description = "수정 시각") LocalDateTime modifiedAt) {}
