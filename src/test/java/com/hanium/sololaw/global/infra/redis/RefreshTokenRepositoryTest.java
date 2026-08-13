/* 
 * Copyright (c) HANIUM SOLOLAW 
 */
package com.hanium.sololaw.global.infra.redis;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.data.redis.core.ValueOperations;

import com.hanium.sololaw.global.config.property.JwtProperties;

import io.jsonwebtoken.MalformedJwtException;

@ExtendWith(MockitoExtension.class)
class RefreshTokenRepositoryTest {

  @Mock private RedisTemplate<String, String> redisTemplate;
  @Mock private ValueOperations<String, String> valueOperations;
  @Mock private SetOperations<String, String> setOperations;

  private RefreshTokenRepository refreshTokenRepository;

  @BeforeEach
  void setUp() {
    JwtProperties jwtProperties =
        new JwtProperties(
            "test-secret-key-test-secret-key-32bytes!",
            3600L,
            3600L,
            2_592_000L,
            false,
            "Lax",
            "RT:",
            "localhost");
    refreshTokenRepository = new RefreshTokenRepository(redisTemplate, jwtProperties);
  }

  @Test
  void saveRefreshToken_withoutUserId_setsValueWithTtlOnly() {
    when(redisTemplate.opsForValue()).thenReturn(valueOperations);

    refreshTokenRepository.saveRefreshToken("token-1", "jti-1", 3600L);

    verify(valueOperations).set("RT:jti-1", "token-1", 3600L, TimeUnit.SECONDS);
  }

  @Test
  void saveRefreshToken_withUserId_alsoIndexesAndRefreshesSetTtl() {
    when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    when(redisTemplate.opsForSet()).thenReturn(setOperations);

    refreshTokenRepository.saveRefreshToken("token-1", "jti-1", 3600L, 1L);

    verify(valueOperations).set("RT:jti-1", "token-1", 3600L, TimeUnit.SECONDS);
    verify(setOperations).add("USER_RT:1", "jti-1");
    verify(redisTemplate).expire("USER_RT:1", 2_592_000L, TimeUnit.SECONDS);
  }

  @Test
  void deleteRefreshToken_withoutUserId_deletesKeyOnly() {
    refreshTokenRepository.deleteRefreshToken("jti-1");

    verify(redisTemplate).delete("RT:jti-1");
    verify(redisTemplate, never()).opsForSet();
  }

  @Test
  void deleteRefreshToken_withUserId_alsoRemovesFromIndex() {
    when(redisTemplate.opsForSet()).thenReturn(setOperations);

    refreshTokenRepository.deleteRefreshToken("jti-1", 1L);

    verify(redisTemplate).delete("RT:jti-1");
    verify(setOperations).remove("USER_RT:1", "jti-1");
  }

  @Test
  void deleteAllRefreshTokensByUser_deletesAllMemberKeysAndIndexSet() {
    when(redisTemplate.opsForSet()).thenReturn(setOperations);
    when(setOperations.members("USER_RT:1")).thenReturn(Set.of("jti-1", "jti-2"));

    refreshTokenRepository.deleteAllRefreshTokensByUser(1L);

    ArgumentCaptor<Collection<String>> keysCaptor = ArgumentCaptor.forClass(Collection.class);
    verify(redisTemplate).delete(keysCaptor.capture());
    assertThat(keysCaptor.getValue()).containsExactlyInAnyOrder("RT:jti-1", "RT:jti-2");
    verify(redisTemplate).delete("USER_RT:1");
  }

  @Test
  void deleteAllRefreshTokensByUser_skipsBulkDelete_whenNoMembers() {
    when(redisTemplate.opsForSet()).thenReturn(setOperations);
    when(setOperations.members("USER_RT:1")).thenReturn(Collections.emptySet());

    refreshTokenRepository.deleteAllRefreshTokensByUser(1L);

    verify(redisTemplate, never()).delete(org.mockito.ArgumentMatchers.<Collection<String>>any());
    verify(redisTemplate).delete("USER_RT:1");
  }

  @Test
  void validateStoredRefreshToken_passesWhenTokenMatches() {
    when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    when(valueOperations.get("RT:jti-1")).thenReturn("token-1");

    refreshTokenRepository.validateStoredRefreshToken("token-1", "jti-1");
  }

  @Test
  void validateStoredRefreshToken_throwsWhenTokenMismatches() {
    when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    when(valueOperations.get("RT:jti-1")).thenReturn("different-token");

    assertThatThrownBy(() -> refreshTokenRepository.validateStoredRefreshToken("token-1", "jti-1"))
        .isInstanceOf(MalformedJwtException.class);
  }
}
