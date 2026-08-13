/* 
 * Copyright (c) HANIUM SOLOLAW 
 */
package com.hanium.sololaw.domain.user.dto.request;

import jakarta.validation.constraints.NotBlank;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "회원 탈퇴 요청 DTO")
public record WithdrawRequest(
    @NotBlank @Schema(description = "비밀번호(재확인)", example = "password1234") String password) {}
