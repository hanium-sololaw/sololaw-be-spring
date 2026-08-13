/* 
 * Copyright (c) HANIUM SOLOLAW 
 */
package com.hanium.sololaw.global.security.resolver;

import org.springframework.core.MethodParameter;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import com.hanium.sololaw.domain.auth.exception.AuthErrorCode;
import com.hanium.sololaw.domain.user.entity.User;
import com.hanium.sololaw.domain.user.exception.UserErrorCode;
import com.hanium.sololaw.domain.user.repository.UserRepository;
import com.hanium.sololaw.global.exception.CustomException;
import com.hanium.sololaw.global.security.annotation.CurrentUser;

import lombok.RequiredArgsConstructor;

/**
 * {@code @CurrentUser}가 붙은 컨트롤러 파라미터에 로그인한 사용자(User 엔티티)를 주입하는 리졸버입니다.
 *
 * <p>SecurityContext의 인증 정보(loginId)로 User를 재조회합니다. open-in-view가 꺼져 있어 이 시점에 조회된 User는 detached
 * 상태이므로, 서비스 계층에서 값을 변경한 뒤에는 반드시 저장소에 명시적으로 저장해야 합니다.
 */
@Component
@RequiredArgsConstructor
public class CurrentUserArgumentResolver implements HandlerMethodArgumentResolver {

  private final UserRepository userRepository;

  @Override
  public boolean supportsParameter(MethodParameter parameter) {
    return parameter.hasParameterAnnotation(CurrentUser.class)
        && parameter.getParameterType().equals(User.class);
  }

  @Override
  public Object resolveArgument(
      MethodParameter parameter,
      ModelAndViewContainer mavContainer,
      NativeWebRequest webRequest,
      WebDataBinderFactory binderFactory) {

    /*
       (1) SecurityContext에서 인증 정보 확인
       - 인증 정보가 없거나 anonymous면 UNAUTHORIZED_TOKEN 예외를 발생시킨다.
       - AnonymousAuthenticationToken은 isAuthenticated()가 항상 true이므로 별도로 걸러야 한다
         (토큰을 아예 안 보낸 요청은 JwtAuthenticationFilter가 거부하지 않고 통과시키므로 실제로 도달 가능하다).
    */
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication == null
        || !authentication.isAuthenticated()
        || authentication instanceof AnonymousAuthenticationToken) {
      throw new CustomException(AuthErrorCode.UNAUTHORIZED_TOKEN);
    }

    /*
       (2) loginId로 사용자 재조회
       - 존재하지 않으면 USER_NOT_FOUND 예외를 발생시킨다.
    */
    String loginId = authentication.getName();
    return userRepository
        .findByLoginId(loginId)
        .orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));
  }
}
