/* 
 * Copyright (c) HANIUM SOLOLAW 
 */
package com.hanium.sololaw.domain.auth.controller;

import java.util.Map;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.hanium.sololaw.domain.auth.dto.request.LoginRequest;
import com.hanium.sololaw.domain.auth.dto.request.SignUpRequest;
import com.hanium.sololaw.domain.auth.dto.result.TokenResult;
import com.hanium.sololaw.domain.auth.exception.AuthErrorCode;
import com.hanium.sololaw.domain.auth.service.AuthService;
import com.hanium.sololaw.global.common.BaseResponse;
import com.hanium.sololaw.global.exception.CustomException;
import com.hanium.sololaw.global.security.jwt.JwtCookieWriter;
import com.hanium.sololaw.global.security.jwt.JwtProvider;
import com.hanium.sololaw.global.security.jwt.TokenType;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
@Tag(name = "Auth", description = "사용자 인증 및 검증 관련 기능을 제공하는 API")
public class AuthController {

  private final AuthService authService;
  private final JwtCookieWriter jwtCookieWriter;
  private final JwtProvider jwtProvider;

  @Operation(
      summary = "[ 사용자 | 토큰 X | 회원가입 ]",
      description =
          """
            **Parameters**  \n
            name: 사용자 이름 \n
            email: 사용자 이메일 \n
            loginId: 사용자 로그인 아이디 \n
            password: 사용자 비밀번호 (8자 이상, 영문+숫자 조합) \n
            agreeToTerms: 서비스 이용약관 동의 여부 \n
            \n
            **Returns**  \n
            회원가입 성공 여부
            """)
  @PostMapping("/register")
  public ResponseEntity<BaseResponse<Void>> register(@Valid @RequestBody SignUpRequest request) {
    authService.signUp(request);
    return ResponseEntity.status(201).body(BaseResponse.success(201, "회원가입에 성공했습니다.", null));
  }

  @Operation(
      summary = "[ 사용자 | 토큰 X | 로그인 ]",
      description =
          """
            **Parameters**  \n
            loginId: 사용자 로그인 아이디 \n
            password: 사용자 비밀번호 \n
            rememberMe: 자동 로그인 여부 \n
            \n
            **Returns (쿠키에 전달)**  \n
            ACCESS_TOKEN: JWT 액세스 토큰 \n
            REFRESH_TOKEN: JWT 리프레시 토큰 \n
            """)
  @PostMapping("/login")
  public ResponseEntity<BaseResponse<Void>> login(@Valid @RequestBody LoginRequest request) {
    TokenResult tokenResponse = authService.login(request);
    HttpHeaders tokenHeaders = new HttpHeaders();
    tokenHeaders.add(
        HttpHeaders.SET_COOKIE,
        jwtCookieWriter.addAccessTokenToCookie(tokenResponse.getAccessToken()).toString());
    tokenHeaders.add(
        HttpHeaders.SET_COOKIE,
        jwtCookieWriter
            .addRefreshTokenToCookie(
                tokenResponse.getRefreshToken(), tokenResponse.getRefreshTokenTtlSeconds())
            .toString());
    return ResponseEntity.status(200)
        .headers(tokenHeaders)
        .body(BaseResponse.success(200, "로그인에 성공했습니다.", null));
  }

  @Operation(
      summary = "[ 사용자 | 토큰 O | 토큰 재발급 ]",
      description =
          """
            **Returns (쿠키에 전달)**  \n
            ACCESS_TOKEN: JWT 액세스 토큰 \n
            REFRESH_TOKEN: JWT 리프레시 토큰 \n
            """)
  @PostMapping("/refresh")
  public ResponseEntity<BaseResponse<Void>> refresh(HttpServletRequest request) {
    String refreshToken = jwtProvider.extractRefreshToken(request);
    validateRefreshToken(refreshToken);
    TokenResult tokenResponse = authService.refresh(refreshToken);
    HttpHeaders tokenHeaders = new HttpHeaders();
    tokenHeaders.add(
        HttpHeaders.SET_COOKIE,
        jwtCookieWriter.addAccessTokenToCookie(tokenResponse.getAccessToken()).toString());
    tokenHeaders.add(
        HttpHeaders.SET_COOKIE,
        jwtCookieWriter
            .addRefreshTokenToCookie(
                tokenResponse.getRefreshToken(), tokenResponse.getRefreshTokenTtlSeconds())
            .toString());
    return ResponseEntity.status(200)
        .headers(tokenHeaders)
        .body(BaseResponse.success(200, "토큰 재발급에 성공했습니다.", null));
  }

  @Operation(
      summary = "[ 사용자 | 토큰 O | 로그아웃 ]",
      description =
          """
            **Returns (쿠키에 전달)**  \n
            ACCESS_TOKEN: 0초 후 만료되는 ACCESS_TOKEN \n
            REFRESH_TOKEN: 0초 후 만료되는 REFRESH_TOKEN \n
            """)
  @PostMapping("/logout")
  public ResponseEntity<BaseResponse<Void>> logout(HttpServletRequest request) {
    String refreshToken = jwtProvider.extractRefreshToken(request);
    validateRefreshToken(refreshToken);
    authService.logout(refreshToken);
    HttpHeaders tokenHeaders = new HttpHeaders();
    tokenHeaders.add(
        HttpHeaders.SET_COOKIE,
        jwtCookieWriter.removeTokenFromCookie(TokenType.ACCESS_TOKEN).toString());
    tokenHeaders.add(
        HttpHeaders.SET_COOKIE,
        jwtCookieWriter.removeTokenFromCookie(TokenType.REFRESH_TOKEN).toString());

    return ResponseEntity.status(200)
        .headers(tokenHeaders)
        .body(BaseResponse.success(200, "로그아웃에 성공하였습니다.", null));
  }

  @Operation(
      summary = "[ 사용자 | 토큰 X | 로그인 아이디 중복 확인 ]",
      description =
          """
            **Parameters**  \n
            loginId: 중복 확인할 로그인 아이디 \n
            \n
            **Returns**  \n
            available: 사용 가능 여부 (true: 사용 가능, false: 이미 존재)
            """)
  @GetMapping("/check-login-id")
  public ResponseEntity<BaseResponse<Map<String, Boolean>>> checkLoginId(
      @RequestParam String loginId) {
    boolean available = authService.checkLoginIdAvailable(loginId);
    return ResponseEntity.ok(BaseResponse.success(Map.of("available", available)));
  }

  @Operation(
      summary = "[ 사용자 | 토큰 X | 이메일 중복 확인 ]",
      description =
          """
            **Parameters**  \n
            email: 중복 확인할 이메일 \n
            \n
            **Returns**  \n
            available: 사용 가능 여부 (true: 사용 가능, false: 이미 존재)
            """)
  @GetMapping("/check-email")
  public ResponseEntity<BaseResponse<Map<String, Boolean>>> checkEmail(@RequestParam String email) {
    boolean available = authService.checkEmailAvailable(email);
    return ResponseEntity.ok(BaseResponse.success(Map.of("available", available)));
  }

  private void validateRefreshToken(String refreshToken) {
    if (!jwtProvider.validateToken(refreshToken, TokenType.REFRESH_TOKEN)) {
      log.info("[AuthController] 유효하지 않은 리프레시 토큰을 통한 리프레시 요청");
      throw new CustomException(AuthErrorCode.UNAUTHORIZED_TOKEN);
    }
  }
}
