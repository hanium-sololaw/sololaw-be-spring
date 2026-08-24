/* 
 * Copyright (c) HANIUM SOLOLAW 
 */
package com.hanium.sololaw.domain.notification.controller;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hanium.sololaw.domain.notification.dto.request.UpdateNotificationSettingRequest;
import com.hanium.sololaw.domain.notification.dto.response.NotificationSettingResponse;
import com.hanium.sololaw.domain.notification.service.NotificationSettingService;
import com.hanium.sololaw.domain.user.entity.User;
import com.hanium.sololaw.global.common.BaseResponse;
import com.hanium.sololaw.global.security.annotation.CurrentUser;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/notification-settings")
@Tag(name = "NotificationSetting", description = "알림 설정 관련 기능을 제공하는 API")
public class NotificationSettingController {

  private final NotificationSettingService notificationSettingService;

  @Operation(
      summary = "[ 사용자 | 토큰 O | 알림 설정 조회 ]",
      description =
          """
            **Returns**  \n
            hearingReminderAlert(변론기일 리마인더), submissionDeadlineAlert(제출 기한 알림), \
            aiPrecedentAlert(유사 판례 업데이트), overdueDeadlineAlert(기한 지남 알림), \
            evidenceSupplementAlert(증거 보완 알림) \n
            \n
            알림 설정은 회원가입 시 기본값으로 생성되어 항상 존재합니다.
            """)
  @GetMapping
  public ResponseEntity<BaseResponse<NotificationSettingResponse>> getMySettings(
      @CurrentUser User user) {
    NotificationSettingResponse result = notificationSettingService.getMySettings(user);
    return ResponseEntity.ok(BaseResponse.success(result));
  }

  @Operation(
      summary = "[ 사용자 | 토큰 O | 알림 설정 수정 ]",
      description =
          """
            **Parameters**  \n
            hearingReminderAlert, submissionDeadlineAlert, aiPrecedentAlert, overdueDeadlineAlert, \
            evidenceSupplementAlert \n
            (null인 필드는 변경하지 않음) \n
            \n
            **Returns**  \n
            수정된 알림 설정
            """)
  @PatchMapping
  public ResponseEntity<BaseResponse<NotificationSettingResponse>> updateMySettings(
      @CurrentUser User user, @Valid @RequestBody UpdateNotificationSettingRequest request) {
    NotificationSettingResponse result = notificationSettingService.updateMySettings(user, request);
    return ResponseEntity.ok(BaseResponse.success(result));
  }
}
