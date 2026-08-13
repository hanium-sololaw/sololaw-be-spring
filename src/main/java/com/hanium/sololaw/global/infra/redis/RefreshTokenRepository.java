/* 
 * Copyright (c) HANIUM SOLOLAW 
 */
package com.hanium.sololaw.global.infra.redis;

import java.util.Set;
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

  private static final String USER_REFRESH_TOKEN_INDEX_PREFIX = "USER_RT:";

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
    redisTemplate.opsForValue().set(refreshKey(jti), token, ttlSeconds, TimeUnit.SECONDS);
  }

  /**
   * 리프레시 토큰을 저장하면서 사용자 단위 인덱스(USER_RT:{userId})에도 함께 등록합니다.
   *
   * @param token 리프레시 토큰
   * @param jti 토큰 고유 식별자
   * @param ttlSeconds TTL(초)
   * @param userId 토큰 소유자 ID
   */
  public void saveRefreshToken(String token, String jti, long ttlSeconds, Long userId) {
    saveRefreshToken(token, jti, ttlSeconds);

    /*
       (1) 사용자 단위 인덱스(USER_RT:{userId})에 jti 등록
       - 세트 자체 TTL은 rememberMe 최댓값(30일)으로 맞춰, 개별 RT보다 세트가 먼저 만료되지 않게 한다.
    */
    String indexKey = userIndexKey(userId);
    redisTemplate.opsForSet().add(indexKey, jti);
    redisTemplate.expire(
        indexKey, jwtProperties.getRefreshTokenLongValidityInSeconds(), TimeUnit.SECONDS);
  }

  public void deleteRefreshToken(String jti) {
    redisTemplate.delete(refreshKey(jti));
  }

  /**
   * 리프레시 토큰을 삭제하면서 사용자 단위 인덱스(USER_RT:{userId})에서도 함께 제거합니다.
   *
   * @param jti 삭제할 토큰의 고유 식별자
   * @param userId 토큰 소유자 ID
   */
  public void deleteRefreshToken(String jti, Long userId) {
    deleteRefreshToken(jti);
    redisTemplate.opsForSet().remove(userIndexKey(userId), jti);
  }

  /**
   * 사용자가 보유한 모든 리프레시 토큰을 일괄 무효화합니다(회원 탈퇴/전체 로그아웃용).
   *
   * @param userId 무효화할 사용자 ID
   */
  public void deleteAllRefreshTokensByUser(Long userId) {
    log.info(
        "[RefreshTokenRepository] deleteAllRefreshTokensByUser() - START | userId: {}", userId);

    /*
       (1) 사용자 단위 인덱스(USER_RT:{userId})에서 jti 목록 조회
    */
    String indexKey = userIndexKey(userId);
    Set<String> jtis = redisTemplate.opsForSet().members(indexKey);

    /*
       (2) 각 리프레시 토큰(RT:{jti}) 삭제
    */
    if (jtis != null && !jtis.isEmpty()) {
      redisTemplate.delete(jtis.stream().map(this::refreshKey).toList());
    }

    /*
       (3) 인덱스 세트 자체 삭제
    */
    redisTemplate.delete(indexKey);

    log.info("[RefreshTokenRepository] deleteAllRefreshTokensByUser() - END | userId: {}", userId);
  }

  public void validateStoredRefreshToken(String refreshToken, String jti) {
    String storedRefreshToken = redisTemplate.opsForValue().get(refreshKey(jti));
    if (!refreshToken.equals(storedRefreshToken)) {
      throw new MalformedJwtException("[RefreshTokenRepository] Invalid refresh token");
    }
  }

  private String refreshKey(String jti) {
    return jwtProperties.getRefreshTokenPrefix() + jti;
  }

  private String userIndexKey(Long userId) {
    return USER_REFRESH_TOKEN_INDEX_PREFIX + userId;
  }
}
