/* 
 * Copyright (c) HANIUM SOLOLAW 
 */
package com.hanium.sololaw.global.config;

import java.io.IOException;

import jakarta.servlet.DispatcherType;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.hanium.sololaw.global.common.BaseResponse;
import com.hanium.sololaw.global.filter.JwtAuthenticationFilter;
import com.hanium.sololaw.global.filter.MdcFilter;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import tools.jackson.databind.ObjectMapper;

@Slf4j
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

  private final CorsConfig corsConfig;
  private final JwtAuthenticationFilter jwtAuthenticationFilter;
  private final ObjectMapper objectMapper;
  private final MdcFilter mdcFilter;

  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    configureFilters(http);
    configureExceptionHandling(http);
    configureAuthorization(http);
    return http.build();
  }

  /** 필터와 기본 설정 */
  private void configureFilters(HttpSecurity http) throws Exception {
    http.csrf(AbstractHttpConfigurer::disable)
        .cors(cors -> cors.configurationSource(corsConfig.corsConfigurationSource()))
        .sessionManagement(
            session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .addFilterBefore(mdcFilter, UsernamePasswordAuthenticationFilter.class)
        .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
  }

  /** 예외 처리: 권한 부족 처리 */
  private void configureExceptionHandling(HttpSecurity http) throws Exception {
    http.exceptionHandling(
        e ->
            e.accessDeniedHandler(this::handleAccessDenied)
                .authenticationEntryPoint(this::handleUnauthorizedOrForbidden));
  }

  private void handleUnauthorizedOrForbidden(
      HttpServletRequest request,
      HttpServletResponse response,
      org.springframework.security.core.AuthenticationException e)
      throws IOException {
    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
    response.setCharacterEncoding("UTF-8");

    BaseResponse<Object> body = BaseResponse.error(401, "인증이 필요합니다.");

    log.warn("[Auth] Security: 인증 필요: {}, {}", request.getMethod(), request.getRequestURI());
    response.getWriter().write(objectMapper.writeValueAsString(body));
    response.getWriter().flush();
  }

  private void handleAccessDenied(
      HttpServletRequest request, HttpServletResponse response, AccessDeniedException ex)
      throws IOException {
    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
    response.setCharacterEncoding("UTF-8");

    BaseResponse<Object> baseResponse = BaseResponse.error(403, "접근 권한이 없습니다.");

    log.warn("[Auth] Security: 권한 부족: {}, {}", request.getMethod(), request.getRequestURI());
    response.getWriter().write(objectMapper.writeValueAsString(baseResponse));
    response.getWriter().flush();
  }

  /** 권한 설정 */
  private void configureAuthorization(HttpSecurity http) throws Exception {
    http.authorizeHttpRequests(
        auth ->
            auth.requestMatchers(
                    "/swagger-ui/**",
                    "/swagger-ui.html",
                    "/v3/api-docs/**",
                    "/api/swagger-ui/**",
                    "/api/swagger-ui.html",
                    "/api/v3/api-docs/**")
                .permitAll()
                .dispatcherTypeMatchers(DispatcherType.ASYNC)
                .permitAll()
                .requestMatchers("/error", "/favicon.ico")
                .permitAll()
                .requestMatchers("/actuator/health")
                .permitAll()
                .requestMatchers("/api/auth/**")
                .permitAll()
                .anyRequest()
                .authenticated());
  }

  /** 비밀번호 인코더 Bean */
  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  /** 인증 관리자 Bean */
  @Bean
  public AuthenticationManager authenticationManager(
      AuthenticationConfiguration authenticationConfiguration) throws Exception {
    return authenticationConfiguration.getAuthenticationManager();
  }
}
