/* 
 * Copyright (c) HANIUM SOLOLAW 
 */
package com.hanium.sololaw.domain.user.controller;

import jakarta.validation.Valid;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hanium.sololaw.domain.user.dto.request.UpdatePasswordRequest;
import com.hanium.sololaw.domain.user.dto.request.UpdateProfileRequest;
import com.hanium.sololaw.domain.user.dto.request.WithdrawRequest;
import com.hanium.sololaw.domain.user.dto.response.UserResponse;
import com.hanium.sololaw.domain.user.entity.User;
import com.hanium.sololaw.domain.user.service.UserService;
import com.hanium.sololaw.global.common.BaseResponse;
import com.hanium.sololaw.global.security.annotation.CurrentUser;
import com.hanium.sololaw.global.security.jwt.JwtCookieWriter;
import com.hanium.sololaw.global.security.jwt.TokenType;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
@Tag(name = "User", description = "내 프로필 관련 기능을 제공하는 API")
public class UserController {

  private final UserService userService;
  private final JwtCookieWriter jwtCookieWriter;

  @Operation(
      summary = "[ 사용자 | 토큰 O | 내 프로필 조회 ]",
      description =
          """
            **Returns**  \n
            name, email, loginId, role, createdAt
            """)
  @GetMapping("/me")
  public ResponseEntity<BaseResponse<UserResponse>> getMe(@CurrentUser User user) {
    UserResponse result = userService.getMe(user);
    return ResponseEntity.ok(BaseResponse.success(result));
  }

  @Operation(
      summary = "[ 사용자 | 토큰 O | 프로필 수정 ]",
      description =
          """
            **Parameters**  \n
            name: 사용자 이름 \n
            email: 사용자 이메일 \n
            \n
            **Returns**  \n
            수정된 프로필
            """)
  @PatchMapping("/me")
  public ResponseEntity<BaseResponse<UserResponse>> updateProfile(
      @CurrentUser User user, @Valid @RequestBody UpdateProfileRequest request) {
    UserResponse result = userService.updateProfile(user, request);
    return ResponseEntity.ok(BaseResponse.success(result));
  }

  @Operation(
      summary = "[ 사용자 | 토큰 O | 비밀번호 변경 ]",
      description =
          """
            **Parameters**  \n
            currentPassword: 현재 비밀번호 \n
            newPassword: 새 비밀번호 (8자 이상, 영문+숫자 조합) \n
            """)
  @PatchMapping("/me/password")
  public ResponseEntity<BaseResponse<Void>> updatePassword(
      @CurrentUser User user, @Valid @RequestBody UpdatePasswordRequest request) {
    userService.updatePassword(user, request);
    return ResponseEntity.ok(BaseResponse.success(200, "비밀번호가 변경되었습니다.", null));
  }

  @Operation(
      summary = "[ 사용자 | 토큰 O | 회원 탈퇴 ]",
      description =
          """
            **Parameters**  \n
            password: 비밀번호(재확인) \n
            \n
            **Returns (쿠키에 전달)**  \n
            ACCESS_TOKEN: 0초 후 만료되는 ACCESS_TOKEN \n
            REFRESH_TOKEN: 0초 후 만료되는 REFRESH_TOKEN \n
            """)
  @DeleteMapping("/me")
  public ResponseEntity<BaseResponse<Void>> withdraw(
      @CurrentUser User user, @Valid @RequestBody WithdrawRequest request) {
    userService.withdraw(user, request);
    HttpHeaders tokenHeaders = new HttpHeaders();
    tokenHeaders.add(
        HttpHeaders.SET_COOKIE,
        jwtCookieWriter.removeTokenFromCookie(TokenType.ACCESS_TOKEN).toString());
    tokenHeaders.add(
        HttpHeaders.SET_COOKIE,
        jwtCookieWriter.removeTokenFromCookie(TokenType.REFRESH_TOKEN).toString());
    return ResponseEntity.status(200)
        .headers(tokenHeaders)
        .body(BaseResponse.success(200, "회원 탈퇴가 완료되었습니다.", null));
  }
}
