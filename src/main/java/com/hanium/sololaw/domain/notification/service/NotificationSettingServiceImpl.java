/* 
 * Copyright (c) HANIUM SOLOLAW 
 */
package com.hanium.sololaw.domain.notification.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hanium.sololaw.domain.notification.dto.request.UpdateNotificationSettingRequest;
import com.hanium.sololaw.domain.notification.dto.response.NotificationSettingResponse;
import com.hanium.sololaw.domain.notification.entity.NotificationSetting;
import com.hanium.sololaw.domain.notification.exception.NotificationSettingErrorCode;
import com.hanium.sololaw.domain.notification.mapper.NotificationSettingMapper;
import com.hanium.sololaw.domain.notification.repository.NotificationSettingRepository;
import com.hanium.sololaw.domain.user.entity.User;
import com.hanium.sololaw.global.exception.CustomException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationSettingServiceImpl implements NotificationSettingService {

  private final NotificationSettingRepository notificationSettingRepository;
  private final NotificationSettingMapper notificationSettingMapper;

  @Override
  @Transactional(readOnly = true)
  public NotificationSettingResponse getMySettings(User user) {
    log.info("[NotificationSettingService] getMySettings() - START | userId: {}", user.getId());

    /*
       (1) ResponseDto Mapping
       - 회원가입 시점에 AuthServiceImpl이 기본값 행을 생성하므로 항상 존재해야 한다.
    */
    NotificationSettingResponse result =
        notificationSettingMapper.toResponse(getByUserId(user.getId()));

    log.info("[NotificationSettingService] getMySettings() - END | userId: {}", user.getId());
    return result;
  }

  @Override
  @Transactional
  public NotificationSettingResponse updateMySettings(
      User user, UpdateNotificationSettingRequest request) {
    log.info("[NotificationSettingService] updateMySettings() - START | userId: {}", user.getId());

    /*
       (1) 설정 조회 후 부분 수정
       - 같은 트랜잭션 안에서 조회한 managed 엔티티이므로 dirty checking으로 자동 반영된다.
    */
    NotificationSetting setting = getByUserId(user.getId());
    setting.updateSettings(
        request.hearingReminderAlert(),
        request.submissionDeadlineAlert(),
        request.opponentFilingAlert(),
        request.aiPrecedentAlert());

    /*
       (2) ResponseDto Mapping
    */
    NotificationSettingResponse result = notificationSettingMapper.toResponse(setting);

    log.info("[NotificationSettingService] updateMySettings() - END | userId: {}", user.getId());
    return result;
  }

  private NotificationSetting getByUserId(Long userId) {
    return notificationSettingRepository
        .findByUserId(userId)
        .orElseThrow(
            () -> new CustomException(NotificationSettingErrorCode.NOTIFICATION_SETTING_NOT_FOUND));
  }
}
