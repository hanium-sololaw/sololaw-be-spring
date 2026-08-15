/* 
 * Copyright (c) HANIUM SOLOLAW 
 */
package com.hanium.sololaw.domain.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.hanium.sololaw.domain.auth.dto.request.LoginRequest;
import com.hanium.sololaw.domain.auth.dto.request.SignUpRequest;
import com.hanium.sololaw.domain.auth.dto.result.TokenResult;
import com.hanium.sololaw.domain.auth.exception.AuthErrorCode;
import com.hanium.sololaw.domain.auth.mapper.AuthMapper;
import com.hanium.sololaw.domain.notification.entity.NotificationSetting;
import com.hanium.sololaw.domain.notification.repository.NotificationSettingRepository;
import com.hanium.sololaw.domain.precedentsubscription.entity.PrecedentSubscription;
import com.hanium.sololaw.domain.precedentsubscription.entity.enums.PrecedentSearchPlan;
import com.hanium.sololaw.domain.precedentsubscription.repository.PrecedentSubscriptionRepository;
import com.hanium.sololaw.domain.subscription.entity.Subscription;
import com.hanium.sololaw.domain.subscription.entity.enums.StoragePlan;
import com.hanium.sololaw.domain.subscription.entity.enums.SubscriptionStatus;
import com.hanium.sololaw.domain.subscription.repository.SubscriptionRepository;
import com.hanium.sololaw.domain.user.entity.User;
import com.hanium.sololaw.domain.user.repository.UserRepository;
import com.hanium.sololaw.global.config.property.JwtProperties;
import com.hanium.sololaw.global.exception.CustomException;
import com.hanium.sololaw.global.infra.redis.RefreshTokenRepository;
import com.hanium.sololaw.global.security.jwt.JwtProvider;
import com.hanium.sololaw.global.security.jwt.internal.GeneratedRefreshTokenPayload;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

  private static final long FREE_STORAGE_LIMIT_BYTES = 524_288_000L;

  @Mock private JwtProvider jwtProvider;
  @Mock private AuthenticationManager authenticationManager;
  @Mock private RefreshTokenRepository refreshTokenRepository;
  @Mock private UserDetailsService userDetailsService;
  @Mock private UserRepository userRepository;
  @Mock private SubscriptionRepository subscriptionRepository;
  @Mock private PrecedentSubscriptionRepository precedentSubscriptionRepository;
  @Mock private NotificationSettingRepository notificationSettingRepository;
  @Mock private PasswordEncoder passwordEncoder;
  @Mock private AuthMapper authMapper;

  private JwtProperties jwtProperties;
  private AuthServiceImpl authService;

  @BeforeEach
  void setUp() {
    jwtProperties =
        new JwtProperties(
            "test-secret-key-test-secret-key-32bytes!",
            3600L,
            3600L,
            2_592_000L,
            false,
            "Lax",
            "RT:",
            "localhost");
    authService =
        new AuthServiceImpl(
            jwtProvider,
            jwtProperties,
            authenticationManager,
            refreshTokenRepository,
            userDetailsService,
            userRepository,
            subscriptionRepository,
            precedentSubscriptionRepository,
            notificationSettingRepository,
            passwordEncoder,
            authMapper);
  }

  @Test
  void signUp_success_savesUserWithTermsAgreedAtAndCreatesDefaultSubscriptionAndSettings() {
    SignUpRequest request =
        new SignUpRequest("김나경", "user@test.com", "loginId1", "password1234", true);
    when(userRepository.existsByLoginId("loginId1")).thenReturn(false);
    when(userRepository.existsByEmail("user@test.com")).thenReturn(false);
    when(passwordEncoder.encode("password1234")).thenReturn("encodedPw");
    when(userRepository.save(any(User.class)))
        .thenAnswer(
            invocation -> {
              User given = invocation.getArgument(0);
              return User.builder()
                  .id(1L)
                  .name(given.getName())
                  .email(given.getEmail())
                  .loginId(given.getLoginId())
                  .password(given.getPassword())
                  .termsAgreedAt(given.getTermsAgreedAt())
                  .build();
            });

    authService.signUp(request);

    ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
    verify(userRepository).save(userCaptor.capture());
    assertThat(userCaptor.getValue().getTermsAgreedAt()).isNotNull();

    ArgumentCaptor<Subscription> subscriptionCaptor = ArgumentCaptor.forClass(Subscription.class);
    verify(subscriptionRepository).save(subscriptionCaptor.capture());
    Subscription savedSubscription = subscriptionCaptor.getValue();
    assertThat(savedSubscription.getUserId()).isEqualTo(1L);
    assertThat(savedSubscription.getPlan()).isEqualTo(StoragePlan.FREE);
    assertThat(savedSubscription.getStatus()).isEqualTo(SubscriptionStatus.ACTIVE);
    assertThat(savedSubscription.getStorageLimitBytes()).isEqualTo(FREE_STORAGE_LIMIT_BYTES);
    assertThat(savedSubscription.getUsedStorageBytes()).isZero();
    assertThat(savedSubscription.getPriceKrw()).isEqualByComparingTo(BigDecimal.ZERO);

    ArgumentCaptor<PrecedentSubscription> precedentSubscriptionCaptor =
        ArgumentCaptor.forClass(PrecedentSubscription.class);
    verify(precedentSubscriptionRepository).save(precedentSubscriptionCaptor.capture());
    PrecedentSubscription savedPrecedentSubscription = precedentSubscriptionCaptor.getValue();
    assertThat(savedPrecedentSubscription.getUserId()).isEqualTo(1L);
    assertThat(savedPrecedentSubscription.getPlan()).isEqualTo(PrecedentSearchPlan.FREE);
    assertThat(savedPrecedentSubscription.getStatus()).isEqualTo(SubscriptionStatus.ACTIVE);
    assertThat(savedPrecedentSubscription.getPriceKrw()).isEqualByComparingTo(BigDecimal.ZERO);

    ArgumentCaptor<NotificationSetting> settingCaptor =
        ArgumentCaptor.forClass(NotificationSetting.class);
    verify(notificationSettingRepository).save(settingCaptor.capture());
    assertThat(settingCaptor.getValue().getUserId()).isEqualTo(1L);
  }

  @Test
  void signUp_throwsWhenTermsNotAgreed() {
    SignUpRequest request =
        new SignUpRequest("김나경", "user@test.com", "loginId1", "password1234", false);

    assertThatThrownBy(() -> authService.signUp(request))
        .isInstanceOf(CustomException.class)
        .extracting(e -> ((CustomException) e).getErrorCode())
        .isEqualTo(AuthErrorCode.TERMS_NOT_AGREED);

    verify(userRepository, never()).save(any());
  }

  @Test
  void signUp_throwsWhenLoginIdAlreadyExists() {
    SignUpRequest request =
        new SignUpRequest("김나경", "user@test.com", "loginId1", "password1234", true);
    when(userRepository.existsByLoginId("loginId1")).thenReturn(true);

    assertThatThrownBy(() -> authService.signUp(request))
        .isInstanceOf(CustomException.class)
        .extracting(e -> ((CustomException) e).getErrorCode())
        .isEqualTo(AuthErrorCode.ALREADY_EXIST_LOGIN_ID);

    verify(userRepository, never()).save(any());
  }

  @Test
  void signUp_throwsWhenEmailAlreadyExists() {
    SignUpRequest request =
        new SignUpRequest("김나경", "user@test.com", "loginId1", "password1234", true);
    when(userRepository.existsByLoginId("loginId1")).thenReturn(false);
    when(userRepository.existsByEmail("user@test.com")).thenReturn(true);

    assertThatThrownBy(() -> authService.signUp(request))
        .isInstanceOf(CustomException.class)
        .extracting(e -> ((CustomException) e).getErrorCode())
        .isEqualTo(AuthErrorCode.ALREADY_EXIST_EMAIL);

    verify(userRepository, never()).save(any());
  }

  @Test
  void login_success_savesRefreshTokenIndexedByUserId() {
    LoginRequest request = new LoginRequest("loginId1", "password1234", false);
    Authentication authentication = mock(Authentication.class);
    User user = User.builder().id(1L).loginId("loginId1").build();
    TokenResult expectedResult =
        TokenResult.builder()
            .accessToken("access-token")
            .refreshToken("refresh-token")
            .refreshTokenTtlSeconds(3600L)
            .build();

    when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
        .thenReturn(authentication);
    when(jwtProvider.generateAccessToken(authentication)).thenReturn("access-token");
    when(userRepository.findByLoginId("loginId1")).thenReturn(Optional.of(user));
    when(jwtProvider.generateRefreshToken(authentication, false))
        .thenReturn(new GeneratedRefreshTokenPayload("refresh-token", "jti-1"));
    when(authMapper.toResult("access-token", "refresh-token", 3600L)).thenReturn(expectedResult);

    TokenResult result = authService.login(request);

    assertThat(result).isEqualTo(expectedResult);
    verify(refreshTokenRepository).saveRefreshToken("refresh-token", "jti-1", 3600L, 1L);
  }

  @Test
  void login_throwsLoginFailOnBadCredentials() {
    LoginRequest request = new LoginRequest("loginId1", "wrongPassword", false);
    when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
        .thenThrow(new BadCredentialsException("bad credentials"));

    assertThatThrownBy(() -> authService.login(request))
        .isInstanceOf(CustomException.class)
        .extracting(e -> ((CustomException) e).getErrorCode())
        .isEqualTo(AuthErrorCode.LOGIN_FAIL);

    verify(refreshTokenRepository, never())
        .saveRefreshToken(anyString(), anyString(), eq(0L), any());
  }

  @Test
  void logout_deletesTokenIndexedByUserId_whenUserFound() {
    User user = User.builder().id(1L).loginId("loginId1").build();
    when(jwtProvider.getLoginIdFromToken("refresh-token")).thenReturn("loginId1");
    when(jwtProvider.getJtiFromToken("refresh-token")).thenReturn("jti-1");
    when(userRepository.findByLoginId("loginId1")).thenReturn(Optional.of(user));

    authService.logout("refresh-token");

    verify(refreshTokenRepository).deleteRefreshToken("jti-1", 1L);
    verify(refreshTokenRepository, never()).deleteRefreshToken("jti-1");
  }

  @Test
  void logout_fallsBackToJtiOnlyDelete_whenUserNotFound() {
    when(jwtProvider.getLoginIdFromToken("refresh-token")).thenReturn("withdrawnUser");
    when(jwtProvider.getJtiFromToken("refresh-token")).thenReturn("jti-1");
    when(userRepository.findByLoginId("withdrawnUser")).thenReturn(Optional.empty());

    authService.logout("refresh-token");

    verify(refreshTokenRepository).deleteRefreshToken("jti-1");
    verify(refreshTokenRepository, never()).deleteRefreshToken(eq("jti-1"), any());
  }
}
