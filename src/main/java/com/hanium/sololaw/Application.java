/* 
 * Copyright (c) HANIUM SOLOLAW 
 */
package com.hanium.sololaw;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
@ConfigurationPropertiesScan(basePackages = "com.hanium.sololaw.global.config.property")
public class Application {

  public static void main(String[] args) {
    SpringApplication.run(Application.class, args);
  }
}
