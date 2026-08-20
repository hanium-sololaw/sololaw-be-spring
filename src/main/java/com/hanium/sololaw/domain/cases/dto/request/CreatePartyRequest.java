/* 
 * Copyright (c) HANIUM SOLOLAW 
 */
package com.hanium.sololaw.domain.cases.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import com.hanium.sololaw.domain.cases.entity.enums.PartyRole;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "당사자 추가 요청 DTO")
public record CreatePartyRequest(
    @NotNull @Schema(description = "당사자 역할") PartyRole partyRole,
    @NotBlank @Schema(description = "이름/상호", example = "김철수") String name) {}
