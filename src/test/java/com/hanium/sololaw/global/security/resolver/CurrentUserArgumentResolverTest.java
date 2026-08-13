/* 
 * Copyright (c) HANIUM SOLOLAW 
 */
package com.hanium.sololaw.global.security.resolver;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;

import com.hanium.sololaw.domain.auth.exception.AuthErrorCode;
import com.hanium.sololaw.domain.user.entity.User;
import com.hanium.sololaw.domain.user.exception.UserErrorCode;
import com.hanium.sololaw.domain.user.repository.UserRepository;
import com.hanium.sololaw.global.exception.CustomException;

@ExtendWith(MockitoExtension.class)
class CurrentUserArgumentResolverTest {

  @Mock private UserRepository userRepository;

  @InjectMocks private CurrentUserArgumentResolver currentUserArgumentResolver;

  @AfterEach
  void tearDown() {
    SecurityContextHolder.clearContext();
  }

  @Test
  void resolveArgument_throwsUnauthorized_whenNoAuthentication() {
    SecurityContextHolder.getContext().setAuthentication(null);

    assertThatThrownBy(() -> currentUserArgumentResolver.resolveArgument(null, null, null, null))
        .isInstanceOf(CustomException.class)
        .extracting(e -> ((CustomException) e).getErrorCode())
        .isEqualTo(AuthErrorCode.UNAUTHORIZED_TOKEN);
  }

  @Test
  void resolveArgument_throwsUnauthorized_whenAnonymous() {
    SecurityContextHolder.getContext()
        .setAuthentication(
            new AnonymousAuthenticationToken(
                "key", "anonymousUser", AuthorityUtils.createAuthorityList("ROLE_ANONYMOUS")));

    assertThatThrownBy(() -> currentUserArgumentResolver.resolveArgument(null, null, null, null))
        .isInstanceOf(CustomException.class)
        .extracting(e -> ((CustomException) e).getErrorCode())
        .isEqualTo(AuthErrorCode.UNAUTHORIZED_TOKEN);
  }

  @Test
  void resolveArgument_returnsUser_whenAuthenticatedAndFound() {
    User user = User.builder().id(1L).loginId("loginId1").build();
    SecurityContextHolder.getContext()
        .setAuthentication(new UsernamePasswordAuthenticationToken("loginId1", null, List.of()));
    when(userRepository.findByLoginId("loginId1")).thenReturn(Optional.of(user));

    Object result = currentUserArgumentResolver.resolveArgument(null, null, null, null);

    assertThat(result).isEqualTo(user);
  }

  @Test
  void resolveArgument_throwsUserNotFound_whenAuthenticatedButUserMissing() {
    SecurityContextHolder.getContext()
        .setAuthentication(
            new UsernamePasswordAuthenticationToken("withdrawnUser", null, List.of()));
    when(userRepository.findByLoginId("withdrawnUser")).thenReturn(Optional.empty());

    assertThatThrownBy(() -> currentUserArgumentResolver.resolveArgument(null, null, null, null))
        .isInstanceOf(CustomException.class)
        .extracting(e -> ((CustomException) e).getErrorCode())
        .isEqualTo(UserErrorCode.USER_NOT_FOUND);
  }
}
