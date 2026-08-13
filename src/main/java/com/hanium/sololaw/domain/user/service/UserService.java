/* 
 * Copyright (c) HANIUM SOLOLAW 
 */
package com.hanium.sololaw.domain.user.service;

import com.hanium.sololaw.domain.user.dto.request.UpdatePasswordRequest;
import com.hanium.sololaw.domain.user.dto.request.UpdateProfileRequest;
import com.hanium.sololaw.domain.user.dto.request.WithdrawRequest;
import com.hanium.sololaw.domain.user.dto.response.UserResponse;
import com.hanium.sololaw.domain.user.entity.User;

public interface UserService {

  /**
   * [ 내 프로필 조회 메서드 ]
   *
   * @param user 로그인한 사용자(@CurrentUser로 주입)
   * @return 조회된 UserResponse
   */
  UserResponse getMe(User user);

  /**
   * [ 프로필 수정 메서드 ]
   *
   * @param user 로그인한 사용자(@CurrentUser로 주입, detached 상태)
   * @param request 수정할 이름을 담은 요청 객체
   * @return 수정된 UserResponse
   */
  UserResponse updateProfile(User user, UpdateProfileRequest request);

  /**
   * [ 비밀번호 변경 메서드 ]
   *
   * @param user 로그인한 사용자(@CurrentUser로 주입, detached 상태)
   * @param request 현재 비밀번호와 새 비밀번호를 담은 요청 객체
   */
  void updatePassword(User user, UpdatePasswordRequest request);

  /**
   * [ 회원 탈퇴 메서드 ]
   *
   * @param user 로그인한 사용자(@CurrentUser로 주입, detached 상태)
   * @param request 비밀번호 재확인을 담은 요청 객체
   */
  void withdraw(User user, WithdrawRequest request);
}
