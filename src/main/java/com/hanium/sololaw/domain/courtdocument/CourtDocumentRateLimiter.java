/* 
 * Copyright (c) HANIUM SOLOLAW 
 */
package com.hanium.sololaw.domain.courtdocument;

import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import com.hanium.sololaw.domain.courtdocument.exception.CourtDocumentErrorCode;
import com.hanium.sololaw.global.exception.CustomException;

import lombok.RequiredArgsConstructor;

/** 법원 문서 분석은 호출마다 AI API 비용이 발생해 사용자당 분당 호출 횟수를 고정 윈도우로 제한한다. */
@Component
@RequiredArgsConstructor
public class CourtDocumentRateLimiter {

  private static final String RATE_KEY_PREFIX = "CDOC_RATE:";
  private static final long WINDOW_SECONDS = 60;
  private static final long MAX_REQUESTS_PER_WINDOW = 5;

  private final RedisTemplate<String, String> redisTemplate;

  /**
   * @param userId 요청 사용자 ID
   * @throws CustomException 윈도우 내 허용 횟수를 초과하면 {@link CourtDocumentErrorCode#RATE_LIMITED}
   */
  public void checkAndIncrement(Long userId) {
    String key = RATE_KEY_PREFIX + userId;
    Long count = redisTemplate.opsForValue().increment(key);
    if (count != null && count == 1L) {
      redisTemplate.expire(key, WINDOW_SECONDS, TimeUnit.SECONDS);
    }
    if (count != null && count > MAX_REQUESTS_PER_WINDOW) {
      throw new CustomException(CourtDocumentErrorCode.RATE_LIMITED);
    }
  }
}
