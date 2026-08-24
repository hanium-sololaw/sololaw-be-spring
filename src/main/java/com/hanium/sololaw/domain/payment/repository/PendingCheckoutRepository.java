/* 
 * Copyright (c) HANIUM SOLOLAW 
 */
package com.hanium.sololaw.domain.payment.repository;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import tools.jackson.databind.ObjectMapper;

/** 결제 대기 세션을 Redis에 TTL로 저장한다(기존 RefreshTokenRepository와 동일한 패턴). */
@Component
@RequiredArgsConstructor
public class PendingCheckoutRepository {

  private static final String KEY_PREFIX = "PENDING_CHECKOUT:";
  private static final long TTL_SECONDS = 30 * 60;

  private final RedisTemplate<String, String> redisTemplate;
  private final ObjectMapper objectMapper;

  public void save(String orderId, PendingCheckout pendingCheckout) {
    String json = objectMapper.writeValueAsString(pendingCheckout);
    redisTemplate.opsForValue().set(key(orderId), json, TTL_SECONDS, TimeUnit.SECONDS);
  }

  public Optional<PendingCheckout> find(String orderId) {
    String json = redisTemplate.opsForValue().get(key(orderId));
    if (json == null) {
      return Optional.empty();
    }
    return Optional.of(objectMapper.readValue(json, PendingCheckout.class));
  }

  public void delete(String orderId) {
    redisTemplate.delete(key(orderId));
  }

  private String key(String orderId) {
    return KEY_PREFIX + orderId;
  }
}
