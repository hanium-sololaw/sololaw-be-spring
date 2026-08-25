/* 
 * Copyright (c) HANIUM SOLOLAW 
 */
package com.hanium.sololaw.global.config.property;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.AllArgsConstructor;
import lombok.Getter;

/** 송달료 1회분 금액. 법원 재판예규로 주기적으로 바뀌므로 재배포 없이 갱신할 수 있도록 설정값으로 둔다. */
@Getter
@AllArgsConstructor
@ConfigurationProperties("litigation-cost")
public class LitigationCostProperties {

  private long deliveryFeePerUnit;
}
