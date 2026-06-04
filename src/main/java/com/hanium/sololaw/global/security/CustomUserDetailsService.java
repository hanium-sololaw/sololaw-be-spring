/* 
 * Copyright (c) HANIUM SOLOLAW 
 */
package com.hanium.sololaw.global.security;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.hanium.sololaw.domain.user.entity.User;
import com.hanium.sololaw.domain.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

  private final UserRepository userRepository;

  @Override
  public UserDetails loadUserByUsername(String loginId) throws UsernameNotFoundException {
    User user =
        userRepository
            .findByLoginId(loginId)
            .orElseThrow(
                () -> {
                  log.info("[Auth] Security: 해당 아이디를 가진 사용자가 없습니다. - 아이디: {}", loginId);
                  return new UsernameNotFoundException(loginId);
                });
    return new CustomUserDetails(user);
  }
}
