/* 
 * Copyright (c) HANIUM SOLOLAW 
 */
package com.hanium.sololaw.domain.auth.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "회원가입 요청 DTO")
public record SignUpRequest(
    @NotBlank @Schema(description = "사용자 이름", example = "홍길동") String name,
    @NotBlank @Email @Schema(description = "사용자 이메일", example = "user@example.com") String email,
    @NotBlank @Schema(description = "사용자 로그인 아이디", example = "sololaw_user") String loginId,
    @NotBlank
        @Size(min = 8)
        @Pattern(regexp = "^(?=.*[a-zA-Z])(?=.*\\d).+$", message = "영문과 숫자를 조합해주세요")
        @Schema(description = "사용자 비밀번호 (8자 이상, 영문+숫자 조합)", example = "password1234")
        String password,
    @NotNull @Schema(description = "서비스 이용약관 동의 여부", example = "true") Boolean agreeToTerms) {}
