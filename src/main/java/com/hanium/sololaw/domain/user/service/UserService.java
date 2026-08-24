/* 
 * Copyright (c) HANIUM SOLOLAW 
 */
package com.hanium.sololaw.domain.user.service;

import com.hanium.sololaw.domain.user.dto.request.UpdatePasswordRequest;
import com.hanium.sololaw.domain.user.dto.request.UpdateProfileRequest;
import com.hanium.sololaw.domain.user.dto.request.WithdrawRequest;
import com.hanium.sololaw.domain.user.dto.response.UserResponse;
import com.hanium.sololaw.domain.user.dto.result.UpdateProfileResult;
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
   * [ 프로필 수정 메서드 ]. 로그인 아이디가 변경되면 기존 JWT의 subject가 무효해지므로 새 액세스·리프레시 토큰을 함께 발급하고 기존 리프레시 토큰은 모두
   * 무효화한다.
   *
   * @param user 로그인한 사용자(@CurrentUser로 주입, detached 상태)
   * @param request 수정할 이름·이메일·로그인 아이디를 담은 요청 객체
   * @return 수정된 프로필과, 로그인 아이디가 변경된 경우에 한해 새로 발급된 토큰을 담은 UpdateProfileResult
   */
  UpdateProfileResult updateProfile(User user, UpdateProfileRequest request);

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
