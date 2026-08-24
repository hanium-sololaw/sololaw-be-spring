/* 
 * Copyright (c) HANIUM SOLOLAW 
 */
package com.hanium.sololaw.domain.user.dto.result;

import com.hanium.sololaw.domain.user.dto.response.UserResponse;

import lombok.Builder;

/**
 * 프로필 수정 결과 DTO(컨트롤러 내부 전달 전용, API 응답 바디로 직렬화되지 않음). 로그인 아이디가 변경되면 기존 JWT의 subject(loginId)가 더 이상
 * 유효하지 않으므로 새 토큰을 함께 발급한다, 변경되지 않았으면 토큰 필드는 null이다.
 */
@Builder
public record UpdateProfileResult(
    UserResponse profile,
    String newAccessToken,
    String newRefreshToken,
    Long newRefreshTokenTtlSeconds) {}
