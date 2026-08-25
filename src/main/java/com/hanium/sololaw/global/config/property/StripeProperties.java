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

  /** Stripe 대시보드(또는 CLI)에서 발급받은 웹훅 서명 시크릿. 서명 검증에만 쓰이고 API 호출에는 쓰이지 않는다. */
  private String webhookSecret;
}
