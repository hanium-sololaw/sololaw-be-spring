/* 
 * Copyright (c) HANIUM SOLOLAW 
 */
package com.hanium.sololaw.domain.auth.controller;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hanium.sololaw.global.security.jwt.JwtProvider;
import com.hanium.sololaw.global.security.jwt.TokenType;

import io.jsonwebtoken.JwtException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * nginx {@code auth_request}가 호출하는 내부 전용 인증 검증 API입니다. 외부에 노출되지 않아야 하며(인프라 설정으로 별도 보장), 응답은 항상 상태
 * 코드만 사용하고 본문을 갖지 않습니다({@code BaseResponse}를 거치지 않음).
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/internal/auth")
@Tag(name = "Internal", description = "내부 전용(nginx auth_request) API — 외부 노출 금지")
public class InternalAuthController {

  private final JwtProvider jwtProvider;

  @Operation(
      summary = "[ 내부 | nginx auth_request | 세션 검증 ]",
      description =
          """
            **Returns**  \n
            200: 유효한 세션(본문 없음) \n
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
    boolean valid = isValidAccessToken(request);

    log.debug("[InternalAuthController] verify() - END | valid: {}", valid);
    return valid ? ResponseEntity.ok().build() : ResponseEntity.status(401).build();
  }

  private boolean isValidAccessToken(HttpServletRequest request) {
    String accessToken = jwtProvider.extractAccessToken(request);
    if (accessToken == null) {
      return false;
    }
    try {
      return jwtProvider.validateToken(accessToken, TokenType.ACCESS_TOKEN);
    } catch (JwtException e) {
      return false;
    }
  }
}
