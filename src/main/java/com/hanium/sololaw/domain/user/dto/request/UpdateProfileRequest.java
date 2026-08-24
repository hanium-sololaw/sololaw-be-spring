/* 
 * Copyright (c) HANIUM SOLOLAW 
 */
package com.hanium.sololaw.domain.user.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "프로필 수정 요청 DTO")
public record UpdateProfileRequest(
    @NotBlank @Schema(description = "사용자 이름", example = "홍길동") String name,
    @NotBlank @Email @Schema(description = "사용자 이메일", example = "user@example.com") String email,
    @NotBlank @Schema(description = "사용자 로그인 아이디", example = "sololaw_user") String loginId) {}
