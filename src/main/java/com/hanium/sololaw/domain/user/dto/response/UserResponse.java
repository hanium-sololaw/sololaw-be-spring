/* 
 * Copyright (c) HANIUM SOLOLAW 
 */
package com.hanium.sololaw.domain.user.dto.response;

import java.time.LocalDateTime;

import com.hanium.sololaw.domain.user.entity.enums.Role;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
@Schema(description = "내 프로필 응답 DTO")
public record UserResponse(
    @Schema(description = "사용자 이름", example = "홍길동") String name,
    @Schema(description = "사용자 이메일", example = "user@example.com") String email,
    @Schema(description = "사용자 로그인 아이디", example = "sololaw_user") String loginId,
    @Schema(description = "사용자 권한") Role role,
    @Schema(description = "가입 시각") LocalDateTime createdAt) {}
