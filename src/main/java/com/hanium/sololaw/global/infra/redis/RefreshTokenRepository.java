/* 
 * Copyright (c) HANIUM SOLOLAW 
 */
package com.hanium.sololaw.global.infra.redis;

import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import com.hanium.sololaw.global.config.property.JwtProperties;

import io.jsonwebtoken.MalformedJwtException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
@RequiredArgsConstructor
public class RefreshTokenRepository {

  private final RedisTemplate<String, String> redisTemplate;
  private final JwtProperties jwtProperties;

  /**
   * 리프레시 토큰을 Redis에 저장하는 메서드입니다. rememberMe 여부에 따라 TTL이 다릅니다.
   *
   * @param token 리프레시 토큰
   * @param jti 토큰 고유 식별자
   * @param ttlSeconds TTL(초)
   */
  public void saveRefreshToken(String token, String jti, long ttlSeconds) {
    String redisKey = jwtProperties.getRefreshTokenPrefix() + jti;
    redisTemplate.opsForValue().set(redisKey, token, ttlSeconds, TimeUnit.SECONDS);
  }

  public void deleteRefreshToken(String jti) {
    redisTemplate.delete(jwtProperties.getRefreshTokenPrefix() + jti);
  }

  public void validateStoredRefreshToken(String refreshToken, String jti) {
    String storedRefreshToken =
        redisTemplate.opsForValue().get(jwtProperties.getRefreshTokenPrefix() + jti);
    if (!refreshToken.equals(storedRefreshToken)) {
      throw new MalformedJwtException("[RefreshTokenRepository] Invalid refresh token");
    }
  }
}
