/* 
 * Copyright (c) HANIUM SOLOLAW 
 */
package com.hanium.sololaw.domain.notification.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.hanium.sololaw.domain.notification.entity.NotificationSetting;

public interface NotificationSettingRepository extends JpaRepository<NotificationSetting, Long> {

  Optional<NotificationSetting> findByUserId(Long userId);
}
