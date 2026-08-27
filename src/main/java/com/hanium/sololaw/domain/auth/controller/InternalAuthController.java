/* 
 * Copyright (c) HANIUM SOLOLAW 
 */
package com.hanium.sololaw.domain.auth.controller;

import java.util.Optional;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hanium.sololaw.domain.precedentsubscription.entity.enums.PrecedentSearchPlan;
import com.hanium.sololaw.domain.precedentsubscription.service.PrecedentSubscriptionService;
import com.hanium.sololaw.domain.user.entity.User;
import com.hanium.sololaw.domain.user.repository.UserRepository;
import com.hanium.sololaw.global.security.jwt.JwtProvider;
import com.hanium.sololaw.global.security.jwt.TokenType;

import io.jsonwebtoken.JwtException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * nginx {@code auth_request}가 호출하는 내부 전용 인증 검증 API입니다. 외부에 노출되지 않아야 하며(인프라 설정으로 별도 보장), 응답은 항상 상태
 * 코드와 헤더만 사용하고 본문을 갖지 않습니다({@code BaseResponse}를 거치지 않음). 조회 과정에서 어떤 예외가 나든 항상 401로 안전하게 처리한다 —
 * nginx auth_request가 이 API의 응답을 그대로 신뢰하므로 500이나 예상 밖의 응답을 절대 내면 안 된다.
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/internal/auth")
@Tag(name = "Internal", description = "내부 전용(nginx auth_request) API — 외부 노출 금지")
public class InternalAuthController {

  private final JwtProvider jwtProvider;
  private final UserRepository userRepository;
  private final PrecedentSubscriptionService precedentSubscriptionService;

  @Operation(
      summary = "[ 내부 | nginx auth_request | 세션 검증 ]",
      description =
          """
            **Returns**  \n
            200: 유효한 세션(본문 없음, X-User-Id·X-Precedent-Plan 헤더로 사용자 식별자와 \
            판례검색 플랜(FREE/PREMIUM)을 전달) \n
            401: 무효한 세션(본문 없음) \n
            """)
  @PostMapping("/verify")
  public ResponseEntity<Void> verify(HttpServletRequest request) {
    log.debug("[InternalAuthController] verify() - START");

    /*
       (1) 액세스 토큰 추출 및 검증
       - 토큰이 없거나 유효하지 않으면 401을 반환한다.
       - 만료 토큰은 validateToken()이 ExpiredJwtException을 재던지므로 별도로 잡아야 한다
         (안 잡으면 GlobalExceptionHandler가 JSON 바디 500을 반환해 "본문 없음" 요건이 깨진다).
    */
    String accessToken = jwtProvider.extractAccessToken(request);
    if (accessToken == null || !isTokenValid(accessToken)) {
      log.debug("[InternalAuthController] verify() - END | valid: false");
      return ResponseEntity.status(401).build();
    }

    /*
       (2) 사용자·판례검색 플랜 조회 후 헤더 구성
       - 이 API는 nginx가 그대로 신뢰하므로, 조회 중 어떤 예외가 나도(DB 오류 등) 401로 폴백한다.
    */
    try {
      ResponseEntity<Void> response = buildAuthenticatedResponse(accessToken);
      log.debug(
          "[InternalAuthController] verify() - END | valid: {}",
          response.getStatusCode().is2xxSuccessful());
      return response;
    } catch (Exception e) {
      log.warn("[InternalAuthController] verify() - 사용자·플랜 조회 중 예외 발생, 인증 실패로 처리", e);
      return ResponseEntity.status(401).build();
    }
  }

  private boolean isTokenValid(String accessToken) {
    try {
      return jwtProvider.validateToken(accessToken, TokenType.ACCESS_TOKEN);
    } catch (JwtException e) {
      return false;
    }
  }

  private ResponseEntity<Void> buildAuthenticatedResponse(String accessToken) {
    String loginId = jwtProvider.getLoginIdFromToken(accessToken);
    Optional<User> user = userRepository.findByLoginId(loginId);
    if (user.isEmpty()) {
      return ResponseEntity.status(401).build();
    }

    PrecedentSearchPlan plan = precedentSubscriptionService.getEffectivePlan(user.get());
    return ResponseEntity.ok()
        .header("X-User-Id", String.valueOf(user.get().getId()))
        .header("X-Precedent-Plan", plan.name())
        .build();
  }
}
