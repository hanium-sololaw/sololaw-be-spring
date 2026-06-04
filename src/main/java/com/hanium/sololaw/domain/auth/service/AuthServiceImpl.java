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
public class AuthServiceImpl implements AuthService {

  private final JwtProvider jwtProvider;
  private final JwtProperties jwtProperties;
  private final AuthenticationManager authenticationManager;
  private final RefreshTokenRepository refreshTokenRepository;
  private final UserDetailsService userDetailsService;
  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;

  @Override
  @Transactional
  public void signUp(SignUpRequest request) {
    // 약관 동의 여부 확인
    if (!Boolean.TRUE.equals(request.getAgreeToTerms())) {
      throw new CustomException(AuthErrorCode.TERMS_NOT_AGREED);
    }

    // 로그인 아이디 중복 확인
    if (userRepository.existsByLoginId(request.getLoginId())) {
      log.warn("[Auth] 존재하는 아이디 입력 - 아이디: {}", request.getLoginId());
      throw new CustomException(AuthErrorCode.ALREADY_EXIST_LOGIN_ID);
    }

    // 이메일 중복 확인
    if (userRepository.existsByEmail(request.getEmail())) {
      log.warn("[Auth] 존재하는 이메일 입력 - 이메일: {}", request.getEmail());
      throw new CustomException(AuthErrorCode.ALREADY_EXIST_EMAIL);
    }

    String encodedPassword = passwordEncoder.encode(request.getPassword());
    User user =
        User.builder()
            .name(request.getName())
            .email(request.getEmail())
            .loginId(request.getLoginId())
            .password(encodedPassword)
            .build();
    User savedUser = userRepository.save(user);

    log.info("[Auth] 신규 사용자 회원가입 - 아이디: {}", savedUser.getLoginId());
  }

  @Override
  @Transactional(readOnly = true)
  @TimeTrace(
      methodName = "로그인",
      env = {"local", "dev"})
  public TokenResult login(LoginRequest request) {
    try {
      UsernamePasswordAuthenticationToken authenticationToken =
          new UsernamePasswordAuthenticationToken(request.getLoginId(), request.getPassword());
      Authentication authentication = authenticationManager.authenticate(authenticationToken);

      String accessToken = jwtProvider.generateAccessToken(authentication);

      boolean rememberMe = request.isRememberMe();
      GeneratedRefreshTokenPayload generatedRefreshTokenPayload =
          jwtProvider.generateRefreshToken(authentication, rememberMe);
      String refreshToken = generatedRefreshTokenPayload.token();
      String jti = generatedRefreshTokenPayload.jti();

      // rememberMe 여부에 따라 TTL 결정
      long refreshTokenTtl =
          rememberMe
              ? jwtProperties.getRefreshTokenLongValidityInSeconds()
              : jwtProperties.getRefreshTokenShortValidityInSeconds();

      refreshTokenRepository.saveRefreshToken(refreshToken, jti, refreshTokenTtl);

      TokenResult tokenResponse =
          TokenResult.builder()
              .accessToken(accessToken)
              .refreshToken(refreshToken)
              .refreshTokenTtlSeconds(refreshTokenTtl)
              .build();

      log.info("[AuthService] 사용자 로그인 성공 - 아이디: {}", request.getLoginId());
      return tokenResponse;
    } catch (BadCredentialsException | UsernameNotFoundException e) {
      log.info("[AuthService] 로그인 실패 - 입력한 아이디: {}", request.getLoginId());
      throw new CustomException(AuthErrorCode.LOGIN_FAIL);
    }
  }

  @Override
  @Transactional(readOnly = true)
  public TokenResult refresh(String refreshToken) {
    jwtProvider.validateToken(refreshToken, TokenType.REFRESH_TOKEN);

    String jti = jwtProvider.getJtiFromToken(refreshToken);
    refreshTokenRepository.validateStoredRefreshToken(refreshToken, jti);

    refreshTokenRepository.deleteRefreshToken(jti);

    String loginId = jwtProvider.getLoginIdFromToken(refreshToken);
    UserDetails userDetails = userDetailsService.loadUserByUsername(loginId);
    Authentication authentication =
        new UsernamePasswordAuthenticationToken(loginId, null, userDetails.getAuthorities());

    String newAccessToken = jwtProvider.generateAccessToken(authentication);
    // 리프레시 시에는 기본(short) TTL 사용
    GeneratedRefreshTokenPayload generatedRefreshTokenPayload =
        jwtProvider.generateRefreshToken(authentication, false);
    String newRefreshToken = generatedRefreshTokenPayload.token();

    long refreshTokenTtl = jwtProperties.getRefreshTokenShortValidityInSeconds();
    refreshTokenRepository.saveRefreshToken(
        newRefreshToken, generatedRefreshTokenPayload.jti(), refreshTokenTtl);

    return TokenResult.builder()
        .accessToken(newAccessToken)
        .refreshToken(newRefreshToken)
        .refreshTokenTtlSeconds(refreshTokenTtl)
        .build();
  }

  @Override
  public void logout(String refreshToken) {
    String loginId = jwtProvider.getLoginIdFromToken(refreshToken);
    String jti = jwtProvider.getJtiFromToken(refreshToken);
    refreshTokenRepository.deleteRefreshToken(jti);
    log.info("[AuthService] 사용자 로그아웃 - 아이디: {}", loginId);
  }

  @Override
  @Transactional(readOnly = true)
  public boolean checkLoginIdAvailable(String loginId) {
    return !userRepository.existsByLoginId(loginId);
  }

  @Override
  @Transactional(readOnly = true)
  public boolean checkEmailAvailable(String email) {
    return !userRepository.existsByEmail(email);
  }
}
