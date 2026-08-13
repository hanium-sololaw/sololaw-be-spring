/* 
 * Copyright (c) HANIUM SOLOLAW 
 */
package com.hanium.sololaw.domain.auth.service;

import java.time.LocalDateTime;

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
import com.hanium.sololaw.domain.auth.mapper.AuthMapper;
import com.hanium.sololaw.domain.notification.entity.NotificationSetting;
import com.hanium.sololaw.domain.notification.repository.NotificationSettingRepository;
import com.hanium.sololaw.domain.subscription.entity.Subscription;
import com.hanium.sololaw.domain.subscription.repository.SubscriptionRepository;
import com.hanium.sololaw.domain.user.entity.User;
import com.hanium.sololaw.domain.user.exception.UserErrorCode;
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
  private final SubscriptionRepository subscriptionRepository;
  private final NotificationSettingRepository notificationSettingRepository;
  private final PasswordEncoder passwordEncoder;
  private final AuthMapper authMapper;

  @Override
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
       - 약관 동의 시각(termsAgreedAt)을 함께 기록한다.
    */
    String encodedPassword = passwordEncoder.encode(request.password());
    User user =
        User.builder()
            .name(request.name())
            .email(request.email())
            .loginId(request.loginId())
            .password(encodedPassword)
            .termsAgreedAt(LocalDateTime.now())
            .build();
    User savedUser = userRepository.save(user);

    /*
       (4) 구독/알림 설정 기본값 행 생성
       - subscriptions·notification_settings는 1:1 필수 관계이므로 회원가입 시점에 함께 생성한다.
    */
    subscriptionRepository.save(Subscription.createDefault(savedUser.getId()));
    notificationSettingRepository.save(NotificationSetting.createDefault(savedUser.getId()));

    log.info("[AuthService] signUp() - END | userId: {}", savedUser.getId());
  }

  @Override
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
         - rememberMe 여부에 따라 TTL을 다르게 적용하고, USER_RT 인덱스와 함께 Redis에 저장한다.
      */
      User user =
          userRepository
              .findByLoginId(request.loginId())
              .orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));

      boolean rememberMe = request.rememberMe();
      GeneratedRefreshTokenPayload generatedRefreshTokenPayload =
          jwtProvider.generateRefreshToken(authentication, rememberMe);
      String refreshToken = generatedRefreshTokenPayload.token();
      String jti = generatedRefreshTokenPayload.jti();

      long refreshTokenTtl =
          rememberMe
              ? jwtProperties.getRefreshTokenLongValidityInSeconds()
              : jwtProperties.getRefreshTokenShortValidityInSeconds();

      refreshTokenRepository.saveRefreshToken(refreshToken, jti, refreshTokenTtl, user.getId());

      /*
         (4) TokenResult 조립
      */
      TokenResult tokenResult = authMapper.toResult(accessToken, refreshToken, refreshTokenTtl);

      log.info("[AuthService] login() - END | loginId: {}", request.loginId());
      return tokenResult;
    } catch (BadCredentialsException | UsernameNotFoundException e) {
      log.info("[AuthService] 로그인 실패 - 입력한 아이디: {}", request.loginId());
      throw new CustomException(AuthErrorCode.LOGIN_FAIL);
    }
  }

  @Override
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
       (2) 사용자 조회 및 기존 리프레시 토큰 삭제 (재사용 방지)
       - userId는 USER_RT 인덱스 갱신에 사용한다.
    */
    String loginId = jwtProvider.getLoginIdFromToken(refreshToken);
    User user =
        userRepository
            .findByLoginId(loginId)
            .orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));
    refreshTokenRepository.deleteRefreshToken(jti, user.getId());

    /*
       (3) 새 액세스/리프레시 토큰 발급 및 저장
       - 리프레시 시에는 기본(short) TTL을 사용한다.
    */
    UserDetails userDetails = userDetailsService.loadUserByUsername(loginId);
    Authentication authentication =
        new UsernamePasswordAuthenticationToken(loginId, null, userDetails.getAuthorities());

    String newAccessToken = jwtProvider.generateAccessToken(authentication);
    GeneratedRefreshTokenPayload generatedRefreshTokenPayload =
        jwtProvider.generateRefreshToken(authentication, false);
    String newRefreshToken = generatedRefreshTokenPayload.token();

    long refreshTokenTtl = jwtProperties.getRefreshTokenShortValidityInSeconds();
    refreshTokenRepository.saveRefreshToken(
        newRefreshToken, generatedRefreshTokenPayload.jti(), refreshTokenTtl, user.getId());

    TokenResult tokenResult = authMapper.toResult(newAccessToken, newRefreshToken, refreshTokenTtl);

    log.info("[AuthService] refresh() - END | loginId: {}", loginId);
    return tokenResult;
  }

  @Override
  public void logout(String refreshToken) {
    log.info("[AuthService] logout() - START");

    /*
       (1) 리프레시 토큰 삭제
       - USER_RT 인덱스도 함께 정리한다. 사용자를 찾지 못하면(이미 탈퇴 등) jti 단건 삭제로 폴백한다.
    */
    String loginId = jwtProvider.getLoginIdFromToken(refreshToken);
    String jti = jwtProvider.getJtiFromToken(refreshToken);
    userRepository
        .findByLoginId(loginId)
        .ifPresentOrElse(
            user -> refreshTokenRepository.deleteRefreshToken(jti, user.getId()),
            () -> refreshTokenRepository.deleteRefreshToken(jti));

    log.info("[AuthService] logout() - END | loginId: {}", loginId);
  }

  @Override
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

  @Override
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
