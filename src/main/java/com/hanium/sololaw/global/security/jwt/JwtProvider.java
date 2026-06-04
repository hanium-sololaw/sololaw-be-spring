/* 
 * Copyright (c) HANIUM SOLOLAW 
 */
package com.hanium.sololaw.global.security.jwt;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.crypto.SecretKey;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import com.hanium.sololaw.global.config.property.JwtProperties;
import com.hanium.sololaw.global.security.jwt.internal.GeneratedRefreshTokenPayload;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
@RequiredArgsConstructor
public class JwtProvider {

  private final JwtProperties jwtProperties;

  private static final String AUTHORIZATION_HEADER = "Authorization";
  private static final String BEARER_PREFIX = "Bearer ";

  private SecretKey getSigningKey() {
    return Keys.hmacShaKeyFor(jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8));
  }

  public String generateAccessToken(Authentication authentication) {
    Instant now = Instant.now();

    String loginId = authentication.getName();

    List<String> roles =
        authentication.getAuthorities().stream().map(GrantedAuthority::getAuthority).toList();
    long validitySeconds = jwtProperties.getAccessTokenValidityInSeconds();
    Date issuedAt = Date.from(now);
    Date expiration = Date.from(now.plusSeconds(validitySeconds));

    return Jwts.builder()
        .subject(loginId)
        .claim("type", TokenType.ACCESS_TOKEN.name())
        .claim("role", roles)
        .issuedAt(issuedAt)
        .expiration(expiration)
        .signWith(getSigningKey())
        .compact();
  }

  /**
   * 리프레시 토큰을 생성하는 메서드입니다. rememberMe 여부에 따라 만료 시간이 다릅니다.
   *
   * @param authentication 인증 정보
   * @param rememberMe 자동 로그인 여부
   * @return 생성된 리프레시 토큰과 JTI
   */
  public GeneratedRefreshTokenPayload generateRefreshToken(
      Authentication authentication, boolean rememberMe) {
    Instant now = Instant.now();

    String loginId = authentication.getName();

    long validitySeconds =
        rememberMe
            ? jwtProperties.getRefreshTokenLongValidityInSeconds()
            : jwtProperties.getRefreshTokenShortValidityInSeconds();
    Date issuedAt = Date.from(now);
    Date expiration = Date.from(now.plusSeconds(validitySeconds));
    String jti = UUID.randomUUID().toString();

    String token =
        Jwts.builder()
            .subject(loginId)
            .claim("type", TokenType.REFRESH_TOKEN.name())
            .issuedAt(issuedAt)
            .expiration(expiration)
            .id(jti)
            .signWith(getSigningKey())
            .compact();
    return new GeneratedRefreshTokenPayload(token, jti);
  }

  public String getLoginIdFromToken(String token) {
    return extractClaims(token).getSubject();
  }

  public String getJtiFromToken(String token) {
    return extractClaims(token).getId();
  }

  public List<GrantedAuthority> getAuthoritiesFromToken(String token) {
    List<String> roles = extractClaims(token).get("role", List.class);
    return roles.stream().map(SimpleGrantedAuthority::new).collect(Collectors.toList());
  }

  public String extractAccessToken(HttpServletRequest request) {
    String bearerToken = request.getHeader(AUTHORIZATION_HEADER);

    if (bearerToken != null && bearerToken.startsWith(BEARER_PREFIX)) {
      return bearerToken.substring(BEARER_PREFIX.length());
    } else if (request.getCookies() != null) {
      for (Cookie cookie : request.getCookies()) {
        if (TokenType.ACCESS_TOKEN.name().equals(cookie.getName())) {
          return cookie.getValue();
        }
      }
    }
    return null;
  }

  public String extractRefreshToken(HttpServletRequest request) {
    if (request.getCookies() != null) {
      for (Cookie cookie : request.getCookies()) {
        if (TokenType.REFRESH_TOKEN.name().equals(cookie.getName())) {
          return cookie.getValue();
        }
      }
    }
    return null;
  }

  public boolean validateToken(String token, TokenType type) {
    try {
      Claims claims = extractClaims(token);
      String tokenType = claims.get("type", String.class);
      if (!tokenType.equals(type.name())) {
        log.info("[Jwt] 일치하지 않는 토큰 타입.");
        return false;
      }
      return true;
    } catch (SecurityException | MalformedJwtException e) {
      log.info("[Jwt] 잘못된 JWT 서명입니다.");
    } catch (ExpiredJwtException e) {
      log.info("[Jwt] 만료된 JWT 토큰입니다.");
      throw e;
    } catch (UnsupportedJwtException e) {
      log.info("[Jwt] 지원되지 않는 JWT 토큰입니다.");
    } catch (IllegalArgumentException e) {
      log.warn("[Jwt] JWT 토큰이 null 또는 비어있습니다.");
    } catch (JwtException e) {
      log.info("[Jwt] JWT 토큰이 잘못되었습니다.");
    }
    return false;
  }

  private Claims extractClaims(String token) {
    return Jwts.parser().verifyWith(getSigningKey()).build().parseSignedClaims(token).getPayload();
  }
}
