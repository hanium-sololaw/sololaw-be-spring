/* 
 * Copyright (c) HANIUM SOLOLAW 
 */
package com.hanium.sololaw.domain.auth.dto.result;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(title = "TokenResult: 토큰 결과 DTO")
public class TokenResult {

  @Schema(description = "JWT 액세스 토큰")
  private String accessToken;

  @Schema(description = "JWT 리프레시 토큰")
  private String refreshToken;

  @Schema(description = "리프레시 토큰 TTL(초)")
  private long refreshTokenTtlSeconds;
}
