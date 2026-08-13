/* 
 * Copyright (c) HANIUM SOLOLAW 
 */
package com.hanium.sololaw.domain.auth.mapper;

import org.springframework.stereotype.Component;

import com.hanium.sololaw.domain.auth.dto.result.TokenResult;

@Component
public class AuthMapper {

  /**
   * @param accessToken : 발급된 액세스 토큰
   * @param refreshToken : 발급된 리프레시 토큰
   * @param refreshTokenTtlSeconds : 리프레시 토큰 TTL(초)
   * @return : 변환된 TokenResult
   */
  public TokenResult toResult(
      String accessToken, String refreshToken, long refreshTokenTtlSeconds) {
    return TokenResult.builder()
        .accessToken(accessToken)
        .refreshToken(refreshToken)
        .refreshTokenTtlSeconds(refreshTokenTtlSeconds)
        .build();
  }
}
