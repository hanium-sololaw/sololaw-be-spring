/* 
 * Copyright (c) HANIUM SOLOLAW 
 */
package com.hanium.sololaw.global.config.property;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@ConfigurationProperties("jwt")
public class JwtProperties {

  private String secret;
  private long accessTokenValidityInSeconds;
  private long refreshTokenShortValidityInSeconds;
  private long refreshTokenLongValidityInSeconds;
  private boolean secure;
  private String sameSite;
  private String refreshTokenPrefix;
  private String domain;
}
