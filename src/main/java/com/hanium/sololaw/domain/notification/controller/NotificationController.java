/* 
 * Copyright (c) HANIUM SOLOLAW 
 */
package com.hanium.sololaw.domain.notification.controller;

import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.hanium.sololaw.domain.notification.dto.response.NotificationResponse;
import com.hanium.sololaw.domain.notification.entity.enums.NotificationType;
import com.hanium.sololaw.domain.notification.service.NotificationService;
import com.hanium.sololaw.domain.user.entity.User;
import com.hanium.sololaw.global.common.BaseResponse;
import com.hanium.sololaw.global.common.OffsetPageResponse;
import com.hanium.sololaw.global.security.annotation.CurrentUser;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequiredArgsConstructor
@Tag(name = "Notification", description = "알림 관련 기능을 제공하는 API")
public class NotificationController {

  private final NotificationService notificationService;

  @Operation(
      summary = "[ 사용자 | 토큰 O | 알림 목록 조회 ]",
      description = "isRead, type(선택), page(0-base)/size/sort")
  @GetMapping("/api/notifications")
  public ResponseEntity<BaseResponse<OffsetPageResponse<NotificationResponse>>> getList(
      @CurrentUser User user,
      @RequestParam(required = false) Boolean isRead,
      @RequestParam(required = false) NotificationType type,
      @PageableDefault(size = 20) Pageable pageable) {
    OffsetPageResponse<NotificationResponse> result =
        notificationService.getList(user, isRead, type, pageable);
    return ResponseEntity.ok(BaseResponse.success(result));
  }

  @Operation(summary = "[ 사용자 | 토큰 O | 미읽음 알림 개수 조회 ]")
  @GetMapping("/api/notifications/unread-count")
  public ResponseEntity<BaseResponse<Long>> getUnreadCount(@CurrentUser User user) {
    long result = notificationService.getUnreadCount(user);
    return ResponseEntity.ok(BaseResponse.success(result));
  }

  @Operation(summary = "[ 사용자 | 토큰 O | 알림 읽음 처리 ]")
  @PatchMapping("/api/notifications/{notificationId}/read")
  public ResponseEntity<BaseResponse<NotificationResponse>> markAsRead(
      @CurrentUser User user, @PathVariable Long notificationId) {
    NotificationResponse result = notificationService.markAsRead(user, notificationId);
    return ResponseEntity.ok(BaseResponse.success(result));
  }

  @Operation(summary = "[ 사용자 | 토큰 O | 알림 전체 읽음 처리 ]", description = "처리된 건수를 반환합니다.")
  @PatchMapping("/api/notifications/read-all")
  public ResponseEntity<BaseResponse<Integer>> markAllAsRead(@CurrentUser User user) {
    int result = notificationService.markAllAsRead(user);
    return ResponseEntity.ok(BaseResponse.success(result));
  }

  @Operation(summary = "[ 사용자 | 토큰 O | 알림 삭제 ]")
  @DeleteMapping("/api/notifications/{notificationId}")
  public ResponseEntity<BaseResponse<Void>> delete(
      @CurrentUser User user, @PathVariable Long notificationId) {
    notificationService.delete(user, notificationId);
    return ResponseEntity.ok(BaseResponse.success(200, "알림이 삭제되었습니다.", null));
  }
}
