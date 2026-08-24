/* 
 * Copyright (c) HANIUM SOLOLAW 
 */
package com.hanium.sololaw.domain.user.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.hanium.sololaw.domain.user.dto.request.UpdatePasswordRequest;
import com.hanium.sololaw.domain.user.dto.request.UpdateProfileRequest;
import com.hanium.sololaw.domain.user.dto.request.WithdrawRequest;
import com.hanium.sololaw.domain.user.dto.response.UserResponse;
import com.hanium.sololaw.domain.user.dto.result.UpdateProfileResult;
import com.hanium.sololaw.domain.user.entity.User;
import com.hanium.sololaw.domain.user.exception.UserErrorCode;
import com.hanium.sololaw.domain.user.mapper.UserMapper;
import com.hanium.sololaw.domain.user.repository.UserRepository;
import com.hanium.sololaw.global.config.property.JwtProperties;
import com.hanium.sololaw.global.exception.CustomException;
import com.hanium.sololaw.global.infra.redis.RefreshTokenRepository;
import com.hanium.sololaw.global.security.jwt.JwtProvider;
import com.hanium.sololaw.global.security.jwt.internal.GeneratedRefreshTokenPayload;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

  @Mock private UserRepository userRepository;
  @Mock private UserMapper userMapper;
  @Mock private PasswordEncoder passwordEncoder;
  @Mock private RefreshTokenRepository refreshTokenRepository;
  @Mock private UserDetailsService userDetailsService;
  @Mock private JwtProvider jwtProvider;
  @Mock private JwtProperties jwtProperties;

  @InjectMocks private UserServiceImpl userService;

  private User user() {
    return User.builder()
        .id(1L)
        .name("김나경")
        .email("user@test.com")
        .loginId("loginId1")
        .password("encodedPw")
        .build();
  }

  @Test
  void getMe_returnsMappedResponse() {
    User user = user();
    UserResponse expected =
        UserResponse.builder().name("김나경").email("user@test.com").loginId("loginId1").build();
    when(userMapper.toResponse(user)).thenReturn(expected);

    UserResponse result = userService.getMe(user);

    assertThat(result).isEqualTo(expected);
  }

  @Test
  void updateProfile_updatesFieldsAndReissuesTokens_whenLoginIdChanged() {
    User user = user();
    UpdateProfileRequest request =
        new UpdateProfileRequest("김철수", "changed@test.com", "newLoginId");
    UserDetails userDetails =
        org.springframework.security.core.userdetails.User.builder()
            .username("newLoginId")
            .password("encodedPw")
            .authorities(List.of(new SimpleGrantedAuthority("ROLE_USER")))
            .build();
    when(userRepository.existsByEmail("changed@test.com")).thenReturn(false);
    when(userRepository.existsByLoginId("newLoginId")).thenReturn(false);
    when(userRepository.save(user)).thenReturn(user);
    when(userMapper.toResponse(user)).thenReturn(UserResponse.builder().name("김철수").build());
    when(userDetailsService.loadUserByUsername("newLoginId")).thenReturn(userDetails);
    when(jwtProvider.generateAccessToken(any())).thenReturn("newAccessToken");
    when(jwtProvider.generateRefreshToken(any(), org.mockito.ArgumentMatchers.eq(false)))
        .thenReturn(new GeneratedRefreshTokenPayload("newRefreshToken", "jti-1"));
    when(jwtProperties.getRefreshTokenShortValidityInSeconds()).thenReturn(1_209_600L);

    UpdateProfileResult result = userService.updateProfile(user, request);

    assertThat(user.getName()).isEqualTo("김철수");
    assertThat(user.getEmail()).isEqualTo("changed@test.com");
    assertThat(user.getLoginId()).isEqualTo("newLoginId");
    assertThat(result.newAccessToken()).isEqualTo("newAccessToken");
    assertThat(result.newRefreshToken()).isEqualTo("newRefreshToken");
    verify(userRepository).save(user);
    verify(refreshTokenRepository).deleteAllRefreshTokensByUser(1L);
    verify(refreshTokenRepository).saveRefreshToken("newRefreshToken", "jti-1", 1_209_600L, 1L);
  }

  @Test
  void updateProfile_skipsDuplicateChecksAndTokenReissue_whenEmailAndLoginIdUnchanged() {
    User user = user();
    UpdateProfileRequest request = new UpdateProfileRequest("김철수", "user@test.com", "loginId1");
    when(userRepository.save(user)).thenReturn(user);
    when(userMapper.toResponse(user)).thenReturn(UserResponse.builder().name("김철수").build());

    UpdateProfileResult result = userService.updateProfile(user, request);

    assertThat(user.getName()).isEqualTo("김철수");
    assertThat(result.newAccessToken()).isNull();
    verify(userRepository, never()).existsByEmail(any());
    verify(userRepository, never()).existsByLoginId(any());
    verify(userRepository).save(user);
    verify(refreshTokenRepository, never()).deleteAllRefreshTokensByUser(any());
    verifyNoInteractions(userDetailsService, jwtProvider);
  }

  @Test
  void updateProfile_throwsOnDuplicateEmail() {
    User user = user();
    UpdateProfileRequest request = new UpdateProfileRequest("김철수", "taken@test.com", "loginId1");
    when(userRepository.existsByEmail("taken@test.com")).thenReturn(true);

    assertThatThrownBy(() -> userService.updateProfile(user, request))
        .isInstanceOf(CustomException.class)
        .extracting(e -> ((CustomException) e).getErrorCode())
        .isEqualTo(UserErrorCode.ALREADY_EXIST_EMAIL);

    verify(userRepository, never()).save(any());
  }

  @Test
  void updateProfile_throwsOnDuplicateLoginId() {
    User user = user();
    UpdateProfileRequest request = new UpdateProfileRequest("김철수", "user@test.com", "takenLoginId");
    when(userRepository.existsByLoginId("takenLoginId")).thenReturn(true);

    assertThatThrownBy(() -> userService.updateProfile(user, request))
        .isInstanceOf(CustomException.class)
        .extracting(e -> ((CustomException) e).getErrorCode())
        .isEqualTo(UserErrorCode.ALREADY_EXIST_LOGIN_ID);

    verify(userRepository, never()).save(any());
  }

  @Test
  void updatePassword_success_encodesAndSavesDetachedUser() {
    User user = user();
    UpdatePasswordRequest request = new UpdatePasswordRequest("password1234", "newPassword1234");
    when(passwordEncoder.matches("password1234", "encodedPw")).thenReturn(true);
    when(passwordEncoder.encode("newPassword1234")).thenReturn("newEncodedPw");

    userService.updatePassword(user, request);

    assertThat(user.getPassword()).isEqualTo("newEncodedPw");
    verify(userRepository).save(user);
  }

  @Test
  void updatePassword_throwsOnCurrentPasswordMismatch() {
    User user = user();
    UpdatePasswordRequest request = new UpdatePasswordRequest("wrongPassword", "newPassword1234");
    when(passwordEncoder.matches("wrongPassword", "encodedPw")).thenReturn(false);

    assertThatThrownBy(() -> userService.updatePassword(user, request))
        .isInstanceOf(CustomException.class)
        .extracting(e -> ((CustomException) e).getErrorCode())
        .isEqualTo(UserErrorCode.CURRENT_PASSWORD_MISMATCH);

    verify(userRepository, never()).save(any());
  }

  @Test
  void withdraw_success_deletesUserAndAllRefreshTokens() {
    User user = user();
    WithdrawRequest request = new WithdrawRequest("password1234");
    when(passwordEncoder.matches("password1234", "encodedPw")).thenReturn(true);

    userService.withdraw(user, request);

    verify(userRepository).delete(user);
    verify(refreshTokenRepository).deleteAllRefreshTokensByUser(1L);
  }

  @Test
  void withdraw_throwsOnPasswordMismatch() {
    User user = user();
    WithdrawRequest request = new WithdrawRequest("wrongPassword");
    when(passwordEncoder.matches("wrongPassword", "encodedPw")).thenReturn(false);

    assertThatThrownBy(() -> userService.withdraw(user, request))
        .isInstanceOf(CustomException.class)
        .extracting(e -> ((CustomException) e).getErrorCode())
        .isEqualTo(UserErrorCode.WITHDRAW_PASSWORD_MISMATCH);

    verify(userRepository, never()).delete(any());
    verify(refreshTokenRepository, never()).deleteAllRefreshTokensByUser(any());
  }
}
