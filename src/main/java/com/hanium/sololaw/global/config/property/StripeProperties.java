/* 
 * Copyright (c) HANIUM SOLOLAW 
 */
package com.hanium.sololaw.global.config.property;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@ConfigurationProperties("stripe")
public class StripeProperties {

  private String secretKey;
  private String publishableKey;

  /** Checkout Session 완료 후 리다이렉트할 프론트엔드 기본 URL(예: https://naholo-law-test.vercel.app). */
  private String successUrl;

  private String cancelUrl;
}
