/* 
 * Copyright (c) HANIUM SOLOLAW 
 */
package com.hanium.sololaw.global.config.property;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@ConfigurationProperties("gemini")
public class GeminiProperties {

  private String apiKey;
  private String model;
  private String baseUrl;
}
