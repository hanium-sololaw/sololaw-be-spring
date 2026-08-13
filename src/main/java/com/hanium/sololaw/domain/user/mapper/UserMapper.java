/* 
 * Copyright (c) HANIUM SOLOLAW 
 */
package com.hanium.sololaw.domain.user.mapper;

import org.springframework.stereotype.Component;

import com.hanium.sololaw.domain.user.dto.response.UserResponse;
import com.hanium.sololaw.domain.user.entity.User;

@Component
public class UserMapper {

  /**
   * @param user : 변환할 User Entity
   */
  public UserResponse toResponse(User user) {
    return UserResponse.builder()
        .name(user.getName())
        .email(user.getEmail())
        .loginId(user.getLoginId())
        .role(user.getRole())
        .createdAt(user.getCreatedAt())
        .build();
  }
}
