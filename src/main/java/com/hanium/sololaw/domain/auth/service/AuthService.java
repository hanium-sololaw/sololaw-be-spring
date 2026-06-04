/* 
 * Copyright (c) HANIUM SOLOLAW 
 */
package com.hanium.sololaw.domain.auth.service;

import com.hanium.sololaw.domain.auth.dto.request.LoginRequest;
import com.hanium.sololaw.domain.auth.dto.request.SignUpRequest;
import com.hanium.sololaw.domain.auth.dto.result.TokenResult;

public interface AuthService {

  /**
   * [ 사용자 회원가입 메서드 ]
   *
   * @param request 회원가입 요청을 위한 사용자 정보를 담은 요청 객체
   */
  void signUp(SignUpRequest request);

  /**
   * [ 사용자 로그인 메서드 ]
   *
   * @param request 로그인 요청을 위한 아이디, 비밀번호를 담은 요청 객체
   * @return accessToken, refreshToken을 담은 TokenResult 객체
   */
  TokenResult login(LoginRequest request);

  /**
   * [ 사용자 토큰 리프레시 메서드 ]
   *
   * @param refreshToken 토큰 재발급 요청에 사용될 리프레시 토큰
   * @return 재발급된 accessToken, refreshToken을 담은 TokenResult 객체
   */
  TokenResult refresh(String refreshToken);

  /**
   * [ 로그아웃 메서드 ]
   *
   * @param refreshToken 삭제 및 블랙리스트 처리 할 리프레시 토큰
   */
  void logout(String refreshToken);

  /**
   * [ 로그인 아이디 중복 확인 메서드 ]
   *
   * @param loginId 중복 확인할 로그인 아이디
   * @return 사용 가능 여부
   */
  boolean checkLoginIdAvailable(String loginId);

  /**
   * [ 이메일 중복 확인 메서드 ]
   *
   * @param email 중복 확인할 이메일
   * @return 사용 가능 여부
   */
  boolean checkEmailAvailable(String email);
}
