/* 
 * Copyright (c) HANIUM SOLOLAW 
 */
package com.hanium.sololaw.domain.auth.dto.result;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
@Schema(description = "토큰 결과 DTO (컨트롤러 내부 전달 전용, API 응답 바디로 직렬화되지 않음)")
public record TokenResult(
    @Schema(description = "JWT 액세스 토큰") String accessToken,
    @Schema(description = "JWT 리프레시 토큰") String refreshToken,
    @Schema(description = "리프레시 토큰 TTL(초)") long refreshTokenTtlSeconds) {}
