/* 
 * Copyright (c) HANIUM SOLOLAW 
 */
package com.hanium.sololaw.domain.user.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;

import com.hanium.sololaw.domain.user.dto.request.WithdrawRequest;
import com.hanium.sololaw.domain.user.entity.User;
import com.hanium.sololaw.domain.user.service.UserService;
import com.hanium.sololaw.global.common.BaseResponse;
import com.hanium.sololaw.global.security.jwt.JwtCookieWriter;
import com.hanium.sololaw.global.security.jwt.TokenType;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

  @Mock private UserService userService;
  @Mock private JwtCookieWriter jwtCookieWriter;

  @InjectMocks private UserController userController;

  @Test
  void withdraw_clearsAccessAndRefreshTokenCookies() {
    User user = User.builder().id(1L).loginId("loginId1").build();
    WithdrawRequest request = new WithdrawRequest("password1234");
    when(jwtCookieWriter.removeTokenFromCookie(TokenType.ACCESS_TOKEN))
        .thenReturn(ResponseCookie.from("ACCESS_TOKEN", "").maxAge(0).build());
    when(jwtCookieWriter.removeTokenFromCookie(TokenType.REFRESH_TOKEN))
        .thenReturn(ResponseCookie.from("REFRESH_TOKEN", "").maxAge(0).build());

    ResponseEntity<BaseResponse<Void>> response = userController.withdraw(user, request);

    List<String> setCookieHeaders = response.getHeaders().get(HttpHeaders.SET_COOKIE);
    assertThat(setCookieHeaders).isNotNull();
    assertThat(setCookieHeaders)
        .anyMatch(header -> header.startsWith("ACCESS_TOKEN="))
        .anyMatch(header -> header.startsWith("REFRESH_TOKEN="));
  }
}
