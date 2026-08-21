/* 
 * Copyright (c) HANIUM SOLOLAW 
 */
package com.hanium.sololaw.domain.notification.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hanium.sololaw.domain.notification.dto.response.NotificationResponse;
import com.hanium.sololaw.domain.notification.entity.Notification;
import com.hanium.sololaw.domain.notification.entity.enums.NotificationType;
import com.hanium.sololaw.domain.notification.exception.NotificationErrorCode;
import com.hanium.sololaw.domain.notification.mapper.NotificationMapper;
import com.hanium.sololaw.domain.notification.repository.NotificationRepository;
import com.hanium.sololaw.domain.user.entity.User;
import com.hanium.sololaw.global.common.OffsetPageResponse;
import com.hanium.sololaw.global.exception.CustomException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

  private final NotificationRepository notificationRepository;
  private final NotificationMapper notificationMapper;

  @Override
  @Transactional(readOnly = true)
  public OffsetPageResponse<NotificationResponse> getList(
      User user, Boolean isRead, NotificationType type, Pageable pageable) {
    log.info(
        "[NotificationService] getList() - START | userId: {}, isRead: {}, type: {}",
        user.getId(),
        isRead,
        type);

    Page<Notification> pageResult =
        notificationRepository.findAllByUserIdAndFilters(user.getId(), isRead, type, pageable);
    OffsetPageResponse<NotificationResponse> result =
        OffsetPageResponse.of(
            notificationMapper.toResponseList(pageResult.getContent()),
            pageResult.getTotalElements(),
            pageable.getPageNumber(),
            pageable.getPageSize());

    log.info(
        "[NotificationService] getList() - END | totalElements: {}", pageResult.getTotalElements());
    return result;
  }

  @Override
  @Transactional(readOnly = true)
  public long getUnreadCount(User user) {
    return notificationRepository.countByUserIdAndIsReadFalse(user.getId());
  }

  @Override
  @Transactional
  public NotificationResponse markAsRead(User user, Long notificationId) {
    log.info(
        "[NotificationService] markAsRead() - START | userId: {}, notificationId: {}",
        user.getId(),
        notificationId);

    Notification notification = findOwnedNotification(notificationId, user.getId());
    notification.markAsRead();
    NotificationResponse result = notificationMapper.toResponse(notification);

    log.info("[NotificationService] markAsRead() - END | notificationId: {}", notificationId);
    return result;
  }

  @Override
  @Transactional
  public int markAllAsRead(User user) {
    log.info("[NotificationService] markAllAsRead() - START | userId: {}", user.getId());

    int affected = notificationRepository.markAllAsRead(user.getId());

    log.info("[NotificationService] markAllAsRead() - END | affected: {}", affected);
    return affected;
  }

  @Override
  @Transactional
  public void delete(User user, Long notificationId) {
    log.info(
        "[NotificationService] delete() - START | userId: {}, notificationId: {}",
        user.getId(),
        notificationId);

    Notification notification = findOwnedNotification(notificationId, user.getId());
    notificationRepository.delete(notification);

    log.info("[NotificationService] delete() - END | notificationId: {}", notificationId);
  }

  private Notification findOwnedNotification(Long notificationId, Long userId) {
    return notificationRepository
        .findByIdAndUserId(notificationId, userId)
        .orElseThrow(() -> new CustomException(NotificationErrorCode.NOTIFICATION_NOT_FOUND));
  }
}
