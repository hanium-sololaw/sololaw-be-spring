/* 
 * Copyright (c) HANIUM SOLOLAW 
 */
package com.hanium.sololaw.domain.auth.dto.request;

import jakarta.validation.constraints.NotBlank;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
@Schema(title = "LoginRequest: 로그인 요청 DTO")
public class LoginRequest {

  @NotBlank
  @Schema(description = "사용자 로그인 아이디", example = "sololaw_user")
  private String loginId;

  @NotBlank
  @Schema(description = "사용자 비밀번호", example = "password1234")
  private String password;

  @Schema(description = "자동 로그인 여부", example = "false")
  private boolean rememberMe;
}
