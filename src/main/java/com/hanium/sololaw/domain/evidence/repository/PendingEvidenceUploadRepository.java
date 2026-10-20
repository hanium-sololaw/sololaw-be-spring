/* 
 * Copyright (c) HANIUM SOLOLAW 
 */
package com.hanium.sololaw.domain.evidence.repository;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import tools.jackson.databind.ObjectMapper;

/** 증거 업로드 대기 세션을 Redis에 TTL로 저장한다(기존 PendingCheckoutRepository와 동일한 패턴). */
@Component
@RequiredArgsConstructor
public class PendingEvidenceUploadRepository {

  private static final String KEY_PREFIX = "PENDING_EVIDENCE_UPLOAD:";
  private static final long TTL_SECONDS = 10 * 60;

  private final RedisTemplate<String, String> redisTemplate;
  private final ObjectMapper objectMapper;

  public void save(String key, PendingEvidenceUpload pendingEvidenceUpload) {
    String json = objectMapper.writeValueAsString(pendingEvidenceUpload);
    redisTemplate.opsForValue().set(redisKey(key), json, TTL_SECONDS, TimeUnit.SECONDS);
  }

  public Optional<PendingEvidenceUpload> find(String key) {
    String json = redisTemplate.opsForValue().get(redisKey(key));
    if (json == null) {
      return Optional.empty();
    }
    return Optional.of(objectMapper.readValue(json, PendingEvidenceUpload.class));
  }

  public void delete(String key) {
    redisTemplate.delete(redisKey(key));
  }

  private String redisKey(String key) {
    return KEY_PREFIX + key;
  }
}
