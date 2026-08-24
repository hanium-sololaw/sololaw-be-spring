/* 
 * Copyright (c) HANIUM SOLOLAW 
 */
package com.hanium.sololaw.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

/**
 * 외부 API 연동(토스페이먼츠 등)용 RestClient.Builder 빈. Boot의 RestClientAutoConfiguration은 별도 스타터가 있어야 활성화되는데
 * 이 프로젝트는 아직 그 스타터를 쓰지 않으므로 직접 등록한다.
 */
@Configuration
public class RestClientConfig {

  @Bean
  public RestClient.Builder restClientBuilder() {
    return RestClient.builder();
  }
}
