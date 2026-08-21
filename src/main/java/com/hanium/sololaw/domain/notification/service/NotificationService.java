/* 
 * Copyright (c) HANIUM SOLOLAW 
 */
package com.hanium.sololaw.domain.notification.service;

import org.springframework.data.domain.Pageable;

import com.hanium.sololaw.domain.notification.dto.response.NotificationResponse;
import com.hanium.sololaw.domain.notification.entity.enums.NotificationType;
import com.hanium.sololaw.domain.user.entity.User;
import com.hanium.sololaw.global.common.OffsetPageResponse;

public interface NotificationService {

  /**
   * 로그인 사용자의 알림 목록을 조회합니다.
   *
   * @param user : 로그인 사용자
   * @param isRead : 읽음 여부 필터(선택)
   * @param type : 알림 유형 필터(선택)
   * @param pageable : page(0-base)/size/sort 쿼리 파라미터 바인딩
   * @return : OffsetPageResponse<NotificationResponse>
   */
  OffsetPageResponse<NotificationResponse> getList(
      User user, Boolean isRead, NotificationType type, Pageable pageable);

  /**
   * 로그인 사용자의 미읽음 알림 개수를 조회합니다.
   *
   * @param user : 로그인 사용자
   * @return : 미읽음 개수
   */
  long getUnreadCount(User user);

  /**
   * 알림을 읽음 처리합니다.
   *
   * @param user : 로그인 사용자
   * @param notificationId : 읽음 처리할 알림 ID
   * @return : 수정된 NotificationResponse
   */
  NotificationResponse markAsRead(User user, Long notificationId);

  /**
   * 로그인 사용자의 미읽음 알림을 모두 읽음 처리합니다.
   *
   * @param user : 로그인 사용자
   * @return : 처리된 건수
   */
  int markAllAsRead(User user);

  /**
   * 알림을 삭제합니다.
   *
   * @param user : 로그인 사용자
   * @param notificationId : 삭제할 알림 ID
   */
  void delete(User user, Long notificationId);
}
