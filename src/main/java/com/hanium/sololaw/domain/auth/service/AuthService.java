/* 
 * Copyright (c) HANIUM SOLOLAW 
 */
package com.hanium.sololaw.domain.auth.service;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hanium.sololaw.domain.auth.dto.request.LoginRequest;
import com.hanium.sololaw.domain.auth.dto.request.SignUpRequest;
import com.hanium.sololaw.domain.auth.dto.result.TokenResult;
import com.hanium.sololaw.domain.auth.exception.AuthErrorCode;
import com.hanium.sololaw.domain.user.entity.User;
import com.hanium.sololaw.domain.user.repository.UserRepository;
import com.hanium.sololaw.global.annotation.TimeTrace;
import com.hanium.sololaw.global.config.property.JwtProperties;
import com.hanium.sololaw.global.exception.CustomException;
import com.hanium.sololaw.global.infra.redis.RefreshTokenRepository;
import com.hanium.sololaw.global.security.jwt.JwtProvider;
import com.hanium.sololaw.global.security.jwt.TokenType;
import com.hanium.sololaw.global.security.jwt.internal.GeneratedRefreshTokenPayload;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

  private final JwtProvider jwtProvider;
  private final JwtProperties jwtProperties;
  private final AuthenticationManager authenticationManager;
  private final RefreshTokenRepository refreshTokenRepository;
  private final UserDetailsService userDetailsService;
  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;

  /**
   * [ 사용자 회원가입 메서드 ]
   *
   * @param request 회원가입 요청을 위한 사용자 정보를 담은 요청 객체
   */
  @Transactional
  public void signUp(SignUpRequest request) {
    log.info("[AuthService] signUp() - START | loginId: {}", request.loginId());

    /*
       (1) 약관 동의 여부 확인
       - 동의하지 않았으면 TERMS_NOT_AGREED 예외를 발생시킨다.
    */
    if (!Boolean.TRUE.equals(request.agreeToTerms())) {
      throw new CustomException(AuthErrorCode.TERMS_NOT_AGREED);
    }

    /*
       (2) 로그인 아이디/이메일 중복 확인
       - 이미 존재하면 각각 ALREADY_EXIST_LOGIN_ID, ALREADY_EXIST_EMAIL 예외를 발생시킨다.
    */
    if (userRepository.existsByLoginId(request.loginId())) {
      log.warn("[Auth] 존재하는 아이디 입력 - 아이디: {}", request.loginId());
      throw new CustomException(AuthErrorCode.ALREADY_EXIST_LOGIN_ID);
    }
    if (userRepository.existsByEmail(request.email())) {
      log.warn("[Auth] 존재하는 이메일 입력 - 이메일: {}", request.email());
      throw new CustomException(AuthErrorCode.ALREADY_EXIST_EMAIL);
    }

    /*
       (3) 비밀번호 암호화 후 사용자 생성 및 저장
    */
    String encodedPassword = passwordEncoder.encode(request.password());
    User user =
        User.builder()
            .name(request.name())
            .email(request.email())
            .loginId(request.loginId())
            .password(encodedPassword)
            .build();
    User savedUser = userRepository.save(user);

    log.info("[AuthService] signUp() - END | userId: {}", savedUser.getId());
  }

  /**
   * [ 사용자 로그인 메서드 ]
   *
   * @param request 로그인 요청을 위한 아이디, 비밀번호를 담은 요청 객체
   * @return accessToken, refreshToken을 담은 TokenResult 객체
   */
  @Transactional(readOnly = true)
  @TimeTrace(
      methodName = "로그인",
      env = {"local", "dev"})
  public TokenResult login(LoginRequest request) {
    log.info("[AuthService] login() - START | loginId: {}", request.loginId());

    try {
      /*
         (1) 아이디/비밀번호 인증
      */
      UsernamePasswordAuthenticationToken authenticationToken =
          new UsernamePasswordAuthenticationToken(request.loginId(), request.password());
      Authentication authentication = authenticationManager.authenticate(authenticationToken);

      /*
         (2) 액세스 토큰 발급
      */
      String accessToken = jwtProvider.generateAccessToken(authentication);

      /*
         (3) 리프레시 토큰 발급 및 TTL 결정
         - rememberMe 여부에 따라 TTL을 다르게 적용하고 Redis에 저장한다.
      */
      boolean rememberMe = request.rememberMe();
      GeneratedRefreshTokenPayload generatedRefreshTokenPayload =
          jwtProvider.generateRefreshToken(authentication, rememberMe);
      String refreshToken = generatedRefreshTokenPayload.token();
      String jti = generatedRefreshTokenPayload.jti();

      long refreshTokenTtl =
          rememberMe
              ? jwtProperties.getRefreshTokenLongValidityInSeconds()
              : jwtProperties.getRefreshTokenShortValidityInSeconds();

      refreshTokenRepository.saveRefreshToken(refreshToken, jti, refreshTokenTtl);

      /*
         (4) TokenResult 조립
      */
      TokenResult tokenResult =
          TokenResult.builder()
              .accessToken(accessToken)
              .refreshToken(refreshToken)
              .refreshTokenTtlSeconds(refreshTokenTtl)
              .build();

      log.info("[AuthService] login() - END | loginId: {}", request.loginId());
      return tokenResult;
    } catch (BadCredentialsException | UsernameNotFoundException e) {
      log.info("[AuthService] 로그인 실패 - 입력한 아이디: {}", request.loginId());
      throw new CustomException(AuthErrorCode.LOGIN_FAIL);
    }
  }

  /**
   * [ 사용자 토큰 리프레시 메서드 ]
   *
   * @param refreshToken 토큰 재발급 요청에 사용될 리프레시 토큰
   * @return 재발급된 accessToken, refreshToken을 담은 TokenResult 객체
   */
  @Transactional(readOnly = true)
  public TokenResult refresh(String refreshToken) {
    log.info("[AuthService] refresh() - START");

    /*
       (1) 리프레시 토큰 검증
       - 서명·만료 검증 후 Redis에 저장된 값과 일치하는지 확인한다.
    */
    jwtProvider.validateToken(refreshToken, TokenType.REFRESH_TOKEN);
    String jti = jwtProvider.getJtiFromToken(refreshToken);
    refreshTokenRepository.validateStoredRefreshToken(refreshToken, jti);

    /*
       (2) 기존 리프레시 토큰 삭제 (재사용 방지)
    */
    refreshTokenRepository.deleteRefreshToken(jti);

    /*
       (3) 새 액세스/리프레시 토큰 발급 및 저장
       - 리프레시 시에는 기본(short) TTL을 사용한다.
    */
    String loginId = jwtProvider.getLoginIdFromToken(refreshToken);
    UserDetails userDetails = userDetailsService.loadUserByUsername(loginId);
    Authentication authentication =
        new UsernamePasswordAuthenticationToken(loginId, null, userDetails.getAuthorities());

    String newAccessToken = jwtProvider.generateAccessToken(authentication);
    GeneratedRefreshTokenPayload generatedRefreshTokenPayload =
        jwtProvider.generateRefreshToken(authentication, false);
    String newRefreshToken = generatedRefreshTokenPayload.token();

    long refreshTokenTtl = jwtProperties.getRefreshTokenShortValidityInSeconds();
    refreshTokenRepository.saveRefreshToken(
        newRefreshToken, generatedRefreshTokenPayload.jti(), refreshTokenTtl);

    TokenResult tokenResult =
        TokenResult.builder()
            .accessToken(newAccessToken)
            .refreshToken(newRefreshToken)
            .refreshTokenTtlSeconds(refreshTokenTtl)
            .build();

    log.info("[AuthService] refresh() - END | loginId: {}", loginId);
    return tokenResult;
  }

  /**
   * [ 로그아웃 메서드 ]
   *
   * @param refreshToken 삭제 및 블랙리스트 처리 할 리프레시 토큰
   */
  public void logout(String refreshToken) {
    log.info("[AuthService] logout() - START");

    /*
       (1) 리프레시 토큰 삭제
    */
    String loginId = jwtProvider.getLoginIdFromToken(refreshToken);
    String jti = jwtProvider.getJtiFromToken(refreshToken);
    refreshTokenRepository.deleteRefreshToken(jti);

    log.info("[AuthService] logout() - END | loginId: {}", loginId);
  }

  /**
   * [ 로그인 아이디 중복 확인 메서드 ]
   *
   * @param loginId 중복 확인할 로그인 아이디
   * @return 사용 가능 여부
   */
  @Transactional(readOnly = true)
  public boolean checkLoginIdAvailable(String loginId) {
    log.info("[AuthService] checkLoginIdAvailable() - START | loginId: {}", loginId);

    /*
       (1) 로그인 아이디 존재 여부 확인
    */
    boolean available = !userRepository.existsByLoginId(loginId);

    log.info("[AuthService] checkLoginIdAvailable() - END | available: {}", available);
    return available;
  }

  /**
   * [ 이메일 중복 확인 메서드 ]
   *
   * @param email 중복 확인할 이메일
   * @return 사용 가능 여부
   */
  @Transactional(readOnly = true)
  public boolean checkEmailAvailable(String email) {
    log.info("[AuthService] checkEmailAvailable() - START | email: {}", email);

    /*
       (1) 이메일 존재 여부 확인
    */
    boolean available = !userRepository.existsByEmail(email);

    log.info("[AuthService] checkEmailAvailable() - END | available: {}", available);
    return available;
  }
}
