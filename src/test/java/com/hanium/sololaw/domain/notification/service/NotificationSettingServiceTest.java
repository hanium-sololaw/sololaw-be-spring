/* 
 * Copyright (c) HANIUM SOLOLAW 
 */
package com.hanium.sololaw.domain.notification.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.hanium.sololaw.domain.notification.dto.request.UpdateNotificationSettingRequest;
import com.hanium.sololaw.domain.notification.dto.response.NotificationSettingResponse;
import com.hanium.sololaw.domain.notification.entity.NotificationSetting;
import com.hanium.sololaw.domain.notification.exception.NotificationSettingErrorCode;
import com.hanium.sololaw.domain.notification.mapper.NotificationSettingMapper;
import com.hanium.sololaw.domain.notification.repository.NotificationSettingRepository;
import com.hanium.sololaw.domain.user.entity.User;
import com.hanium.sololaw.global.exception.CustomException;

@ExtendWith(MockitoExtension.class)
class NotificationSettingServiceTest {

  @Mock private NotificationSettingRepository notificationSettingRepository;
  @Mock private NotificationSettingMapper notificationSettingMapper;

  @InjectMocks private NotificationSettingServiceImpl notificationSettingService;

  @Test
  void getMySettings_returnsMappedResponse_whenFound() {
    User user = User.builder().id(1L).build();
    NotificationSetting setting = NotificationSetting.builder().id(10L).userId(1L).build();
    NotificationSettingResponse expected =
        NotificationSettingResponse.builder().hearingReminderAlert(true).build();
    when(notificationSettingRepository.findByUserId(1L)).thenReturn(Optional.of(setting));
    when(notificationSettingMapper.toResponse(setting)).thenReturn(expected);

    NotificationSettingResponse result = notificationSettingService.getMySettings(user);

    assertThat(result).isEqualTo(expected);
  }

  @Test
  void getMySettings_throwsNotFound_whenMissing() {
    User user = User.builder().id(1L).build();
    when(notificationSettingRepository.findByUserId(1L)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> notificationSettingService.getMySettings(user))
        .isInstanceOf(CustomException.class)
        .extracting(e -> ((CustomException) e).getErrorCode())
        .isEqualTo(NotificationSettingErrorCode.NOTIFICATION_SETTING_NOT_FOUND);
  }

  @Test
  void updateMySettings_appliesOnlyNonNullFields() {
    User user = User.builder().id(1L).build();
    NotificationSetting setting = NotificationSetting.builder().id(10L).userId(1L).build();
    UpdateNotificationSettingRequest request =
        new UpdateNotificationSettingRequest(null, null, true, null, false);
    when(notificationSettingRepository.findByUserId(1L)).thenReturn(Optional.of(setting));
    when(notificationSettingMapper.toResponse(any(NotificationSetting.class)))
        .thenReturn(NotificationSettingResponse.builder().build());

    notificationSettingService.updateMySettings(user, request);

    assertThat(setting.getAiPrecedentAlert()).isTrue();
    assertThat(setting.getHearingReminderAlert()).isTrue();
    assertThat(setting.getSubmissionDeadlineAlert()).isTrue();
    assertThat(setting.getEvidenceSupplementAlert()).isFalse();
    assertThat(setting.getOverdueDeadlineAlert()).isTrue();
  }

  @Test
  void updateMySettings_throwsNotFound_whenMissing() {
    User user = User.builder().id(1L).build();
    UpdateNotificationSettingRequest request =
        new UpdateNotificationSettingRequest(false, null, null, null, null);
    when(notificationSettingRepository.findByUserId(1L)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> notificationSettingService.updateMySettings(user, request))
        .isInstanceOf(CustomException.class)
        .extracting(e -> ((CustomException) e).getErrorCode())
        .isEqualTo(NotificationSettingErrorCode.NOTIFICATION_SETTING_NOT_FOUND);
  }
}
