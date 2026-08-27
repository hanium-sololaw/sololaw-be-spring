/* 
 * Copyright (c) HANIUM SOLOLAW 
 */
package com.hanium.sololaw.domain.auth.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;

import com.hanium.sololaw.domain.precedentsubscription.entity.enums.PrecedentSearchPlan;
import com.hanium.sololaw.domain.precedentsubscription.service.PrecedentSubscriptionService;
import com.hanium.sololaw.domain.user.entity.User;
import com.hanium.sololaw.domain.user.repository.UserRepository;
import com.hanium.sololaw.global.security.jwt.JwtProvider;
import com.hanium.sololaw.global.security.jwt.TokenType;

import io.jsonwebtoken.JwtException;

@ExtendWith(MockitoExtension.class)
class InternalAuthControllerTest {

  @Mock private JwtProvider jwtProvider;
  @Mock private UserRepository userRepository;
  @Mock private PrecedentSubscriptionService precedentSubscriptionService;

  @InjectMocks private InternalAuthController internalAuthController;

  @Test
  void verify_returns200WithHeaders_whenTokenValidAndUserFoundAndPlanResolved() {
    MockHttpServletRequest request = new MockHttpServletRequest();
    User user = User.builder().id(7L).build();
    when(jwtProvider.extractAccessToken(request)).thenReturn("valid-token");
    when(jwtProvider.validateToken("valid-token", TokenType.ACCESS_TOKEN)).thenReturn(true);
    when(jwtProvider.getLoginIdFromToken("valid-token")).thenReturn("user1");
    when(userRepository.findByLoginId("user1")).thenReturn(Optional.of(user));
    when(precedentSubscriptionService.getEffectivePlan(user))
        .thenReturn(PrecedentSearchPlan.PREMIUM);

    ResponseEntity<Void> response = internalAuthController.verify(request);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isNull();
    assertThat(response.getHeaders().getFirst("X-User-Id")).isEqualTo("7");
    assertThat(response.getHeaders().getFirst("X-Precedent-Plan")).isEqualTo("PREMIUM");
  }

  @Test
  void verify_returns401NoHeaders_whenUserNotFound() {
    MockHttpServletRequest request = new MockHttpServletRequest();
    when(jwtProvider.extractAccessToken(request)).thenReturn("valid-token");
    when(jwtProvider.validateToken("valid-token", TokenType.ACCESS_TOKEN)).thenReturn(true);
    when(jwtProvider.getLoginIdFromToken("valid-token")).thenReturn("ghost-user");
    when(userRepository.findByLoginId("ghost-user")).thenReturn(Optional.empty());

    ResponseEntity<Void> response = internalAuthController.verify(request);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    assertThat(response.getBody()).isNull();
    assertThat(response.getHeaders().getFirst("X-User-Id")).isNull();
  }

  @Test
  void verify_returns401_whenPlanLookupThrowsUnexpectedException() {
    MockHttpServletRequest request = new MockHttpServletRequest();
    User user = User.builder().id(7L).build();
    when(jwtProvider.extractAccessToken(request)).thenReturn("valid-token");
    when(jwtProvider.validateToken("valid-token", TokenType.ACCESS_TOKEN)).thenReturn(true);
    when(jwtProvider.getLoginIdFromToken("valid-token")).thenReturn("user1");
    when(userRepository.findByLoginId("user1")).thenReturn(Optional.of(user));
    when(precedentSubscriptionService.getEffectivePlan(user))
        .thenThrow(new RuntimeException("db down"));

    ResponseEntity<Void> response = internalAuthController.verify(request);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    assertThat(response.getBody()).isNull();
  }

  @Test
  void verify_returns401NoBody_whenNoTokenPresent() {
    MockHttpServletRequest request = new MockHttpServletRequest();
    when(jwtProvider.extractAccessToken(request)).thenReturn(null);

    ResponseEntity<Void> response = internalAuthController.verify(request);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    assertThat(response.getBody()).isNull();
    verify(jwtProvider, never()).validateToken(anyString(), any(TokenType.class));
  }

  @Test
  void verify_returns401NoBody_whenTokenInvalid() {
    MockHttpServletRequest request = new MockHttpServletRequest();
    when(jwtProvider.extractAccessToken(request)).thenReturn("bad-token");
    when(jwtProvider.validateToken("bad-token", TokenType.ACCESS_TOKEN)).thenReturn(false);

    ResponseEntity<Void> response = internalAuthController.verify(request);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    assertThat(response.getBody()).isNull();
  }

  @Test
  void verify_returns401NoBody_whenValidateTokenThrowsJwtException() {
    MockHttpServletRequest request = new MockHttpServletRequest();
    when(jwtProvider.extractAccessToken(request)).thenReturn("expired-token");
    when(jwtProvider.validateToken("expired-token", TokenType.ACCESS_TOKEN))
        .thenThrow(new JwtException("expired"));

    ResponseEntity<Void> response = internalAuthController.verify(request);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    assertThat(response.getBody()).isNull();
  }
}
