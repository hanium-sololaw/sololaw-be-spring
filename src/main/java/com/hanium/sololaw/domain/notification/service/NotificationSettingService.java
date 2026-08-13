/* 
 * Copyright (c) HANIUM SOLOLAW 
 */
package com.hanium.sololaw.domain.notification.service;

import com.hanium.sololaw.domain.notification.dto.request.UpdateNotificationSettingRequest;
import com.hanium.sololaw.domain.notification.dto.response.NotificationSettingResponse;
import com.hanium.sololaw.domain.user.entity.User;

public interface NotificationSettingService {

  /**
   * [ 내 알림 설정 조회 메서드 ]
   *
   * @param user 로그인한 사용자(@CurrentUser로 주입)
   * @return 조회된 NotificationSettingResponse. 설정 행은 회원가입 시점에 기본값으로 생성되어 항상 존재한다.
   */
  NotificationSettingResponse getMySettings(User user);

  /**
   * [ 내 알림 설정 수정 메서드 ]
   *
   * @param user 로그인한 사용자(@CurrentUser로 주입)
   * @param request 수정할 필드를 담은 요청 객체(null 필드는 미변경)
   * @return 수정된 NotificationSettingResponse
   */
  NotificationSettingResponse updateMySettings(User user, UpdateNotificationSettingRequest request);
}
