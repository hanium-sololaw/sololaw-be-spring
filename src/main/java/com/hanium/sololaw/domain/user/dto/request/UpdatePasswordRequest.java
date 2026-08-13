/* 
 * Copyright (c) HANIUM SOLOLAW 
 */
package com.hanium.sololaw.domain.user.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "비밀번호 변경 요청 DTO")
public record UpdatePasswordRequest(
    @NotBlank @Schema(description = "현재 비밀번호", example = "password1234") String currentPassword,
    @NotBlank
        @Size(min = 8)
        @Pattern(regexp = "^(?=.*[a-zA-Z])(?=.*\\d).+$", message = "영문과 숫자를 조합해주세요")
        @Schema(description = "새 비밀번호 (8자 이상, 영문+숫자 조합)", example = "newPassword1234")
        String newPassword) {}
