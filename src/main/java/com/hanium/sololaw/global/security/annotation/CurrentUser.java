/* 
 * Copyright (c) HANIUM SOLOLAW 
 */
package com.hanium.sololaw.global.security.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import io.swagger.v3.oas.annotations.Parameter;

/**
 * 컨트롤러 메서드 파라미터에 로그인한 사용자(User 엔티티)를 주입받기 위한 애노테이션입니다. 인증 컨텍스트에서 서버가 직접 채우는 값이므로 Swagger 문서에는 노출하지
 * 않는다({@code @Parameter(hidden = true)}가 없으면 springdoc이 User 엔티티 전체를 요청 파라미터로 문서화한다).
 *
 * @see com.hanium.sololaw.global.security.resolver.CurrentUserArgumentResolver
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Parameter(hidden = true)
public @interface CurrentUser {}
