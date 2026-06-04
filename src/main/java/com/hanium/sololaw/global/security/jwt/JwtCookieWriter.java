/* 
 * Copyright (c) HANIUM SOLOLAW 
 */
package com.hanium.sololaw.global.security.jwt;

import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import com.hanium.sololaw.global.config.property.JwtProperties;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/** 사용자 로그인, 로그아웃 시 반환할 쿠키를 작성하는 클래스입니다. */
@Component
@RequiredArgsConstructor
@Slf4j
public class JwtCookieWriter {

  private final JwtProperties jwtProperties;

  public ResponseCookie addAccessTokenToCookie(String accessToken) {
    ResponseCookie.ResponseCookieBuilder builder =
        ResponseCookie.from(TokenType.ACCESS_TOKEN.name(), accessToken)
            .httpOnly(true)
            .secure(jwtProperties.isSecure())
            .sameSite(jwtProperties.getSameSite())
            .path("/")
            .maxAge(jwtProperties.getAccessTokenValidityInSeconds());

    if (jwtProperties.getDomain() != null && !jwtProperties.getDomain().isEmpty()) {
      builder.domain(jwtProperties.getDomain());
    }

    return builder.build();
  }

  /**
   * 리프레시 토큰 쿠키를 생성하는 메서드입니다. rememberMe 여부에 따라 maxAge가 달라집니다.
   *
   * @param refreshToken 리프레시 토큰
   * @param maxAge 쿠키 만료 시간(초)
   * @return 생성된 ResponseCookie
   */
  public ResponseCookie addRefreshTokenToCookie(String refreshToken, long maxAge) {
    ResponseCookie.ResponseCookieBuilder builder =
        ResponseCookie.from(TokenType.REFRESH_TOKEN.name(), refreshToken)
            .httpOnly(true)
            .secure(jwtProperties.isSecure())
            .sameSite(jwtProperties.getSameSite())
            .path("/")
            .maxAge(maxAge);

    if (jwtProperties.getDomain() != null && !jwtProperties.getDomain().isEmpty()) {
      builder.domain(jwtProperties.getDomain());
    }

    return builder.build();
  }

  public ResponseCookie removeTokenFromCookie(TokenType tokenType) {
    ResponseCookie.ResponseCookieBuilder builder =
        ResponseCookie.from(tokenType.name(), null)
            .httpOnly(true)
            .secure(jwtProperties.isSecure())
            .sameSite(jwtProperties.getSameSite())
            .path("/")
            .maxAge(0);

    if (jwtProperties.getDomain() != null && !jwtProperties.getDomain().isEmpty()) {
      builder.domain(jwtProperties.getDomain());
    }

    return builder.build();
  }
}
