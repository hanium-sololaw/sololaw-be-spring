/* 
 * Copyright (c) HANIUM SOLOLAW 
 */
package com.hanium.sololaw.domain.notification.mapper;

import org.springframework.stereotype.Component;

import com.hanium.sololaw.domain.notification.dto.response.NotificationSettingResponse;
import com.hanium.sololaw.domain.notification.entity.NotificationSetting;

@Component
public class NotificationSettingMapper {

  /**
   * @param setting : 변환할 NotificationSetting Entity
   */
  public NotificationSettingResponse toResponse(NotificationSetting setting) {
    return NotificationSettingResponse.builder()
        .hearingReminderAlert(setting.getHearingReminderAlert())
        .submissionDeadlineAlert(setting.getSubmissionDeadlineAlert())
        .aiPrecedentAlert(setting.getAiPrecedentAlert())
        .build();
  }
}
