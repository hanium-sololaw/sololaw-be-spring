/* 
 * Copyright (c) HANIUM SOLOLAW 
 */
package com.hanium.sololaw.domain.user.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hanium.sololaw.domain.user.dto.request.UpdatePasswordRequest;
import com.hanium.sololaw.domain.user.dto.request.UpdateProfileRequest;
import com.hanium.sololaw.domain.user.dto.request.WithdrawRequest;
import com.hanium.sololaw.domain.user.dto.response.UserResponse;
import com.hanium.sololaw.domain.user.entity.User;
import com.hanium.sololaw.domain.user.exception.UserErrorCode;
import com.hanium.sololaw.domain.user.mapper.UserMapper;
import com.hanium.sololaw.domain.user.repository.UserRepository;
import com.hanium.sololaw.global.exception.CustomException;
import com.hanium.sololaw.global.infra.redis.RefreshTokenRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

  private final UserRepository userRepository;
  private final UserMapper userMapper;
  private final PasswordEncoder passwordEncoder;
  private final RefreshTokenRepository refreshTokenRepository;

  @Override
  public UserResponse getMe(User user) {
    log.info("[UserService] getMe() - START | userId: {}", user.getId());

    /*
       (1) ResponseDto Mapping
    */
    UserResponse result = userMapper.toResponse(user);

    log.info("[UserService] getMe() - END | userId: {}", user.getId());
    return result;
  }

  @Override
  @Transactional
  public UserResponse updateProfile(User user, UpdateProfileRequest request) {
    log.info("[UserService] updateProfile() - START | userId: {}", user.getId());

    /*
       (1) 이메일 변경 시 중복 확인
       - 본인의 기존 이메일로 그대로 수정하는 경우는 중복 검사 대상에서 제외한다.
    */
    if (!user.getEmail().equals(request.email()) && userRepository.existsByEmail(request.email())) {
      throw new CustomException(UserErrorCode.ALREADY_EXIST_EMAIL);
    }

    /*
       (2) 이름/이메일 변경
       - user는 detached 상태이므로 변경 후 명시적으로 저장한다.
    */
    user.updateName(request.name());
    user.updateEmail(request.email());
    User savedUser = userRepository.save(user);

    /*
       (3) ResponseDto Mapping
    */
    UserResponse result = userMapper.toResponse(savedUser);

    log.info("[UserService] updateProfile() - END | userId: {}", user.getId());
    return result;
  }

  @Override
  @Transactional
  public void updatePassword(User user, UpdatePasswordRequest request) {
    log.info("[UserService] updatePassword() - START | userId: {}", user.getId());

    /*
       (1) 현재 비밀번호 검증
       - 일치하지 않으면 CURRENT_PASSWORD_MISMATCH 예외를 발생시킨다.
    */
    if (!passwordEncoder.matches(request.currentPassword(), user.getPassword())) {
      throw new CustomException(UserErrorCode.CURRENT_PASSWORD_MISMATCH);
    }

    /*
       (2) 새 비밀번호 암호화 후 저장
       - user는 detached 상태이므로 변경 후 명시적으로 저장한다.
    */
    user.updatePassword(passwordEncoder.encode(request.newPassword()));
    userRepository.save(user);

    log.info("[UserService] updatePassword() - END | userId: {}", user.getId());
  }

  @Override
  @Transactional
  public void withdraw(User user, WithdrawRequest request) {
    log.info("[UserService] withdraw() - START | userId: {}", user.getId());

    /*
       (1) 비밀번호 재확인
       - 일치하지 않으면 WITHDRAW_PASSWORD_MISMATCH 예외를 발생시킨다.
    */
    if (!passwordEncoder.matches(request.password(), user.getPassword())) {
      throw new CustomException(UserErrorCode.WITHDRAW_PASSWORD_MISMATCH);
    }

    /*
       (2) 사용자 삭제
       - subscriptions/notification_settings 등은 ON DELETE CASCADE로 연쇄 삭제된다.
    */
    Long userId = user.getId();
    userRepository.delete(user);

    /*
       (3) Redis 리프레시 토큰 일괄 무효화
    */
    refreshTokenRepository.deleteAllRefreshTokensByUser(userId);

    log.info("[UserService] withdraw() - END | userId: {}", userId);
  }
}
