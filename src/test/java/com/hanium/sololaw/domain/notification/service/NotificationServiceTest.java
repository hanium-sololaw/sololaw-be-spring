/* 
 * Copyright (c) HANIUM SOLOLAW 
 */
package com.hanium.sololaw.domain.notification.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import com.hanium.sololaw.domain.notification.dto.response.NotificationResponse;
import com.hanium.sololaw.domain.notification.entity.Notification;
import com.hanium.sololaw.domain.notification.entity.enums.NotificationType;
import com.hanium.sololaw.domain.notification.exception.NotificationErrorCode;
import com.hanium.sololaw.domain.notification.mapper.NotificationMapper;
import com.hanium.sololaw.domain.notification.repository.NotificationRepository;
import com.hanium.sololaw.domain.user.entity.User;
import com.hanium.sololaw.global.common.OffsetPageResponse;
import com.hanium.sololaw.global.exception.CustomException;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

  @Mock private NotificationRepository notificationRepository;
  @Mock private NotificationMapper notificationMapper;

  @InjectMocks private NotificationServiceImpl notificationService;

  @Test
  void getList_returnsPagedResponse() {
    User user = User.builder().id(1L).build();
    Notification notification = Notification.builder().id(1L).userId(1L).build();
    PageRequest pageable = PageRequest.of(0, 20);
    when(notificationRepository.findAllByUserIdAndFilters(1L, false, null, pageable))
        .thenReturn(new PageImpl<>(List.of(notification), pageable, 1));
    when(notificationMapper.toResponseList(List.of(notification)))
        .thenReturn(List.of(NotificationResponse.builder().id(1L).build()));

    OffsetPageResponse<NotificationResponse> result =
        notificationService.getList(user, false, null, pageable);

    assertThat(result.totalElements()).isEqualTo(1);
    assertThat(result.content()).hasSize(1);
  }

  @Test
  void getUnreadCount_returnsCountFromRepository() {
    User user = User.builder().id(1L).build();
    when(notificationRepository.countByUserIdAndIsReadFalse(1L)).thenReturn(3L);

    long result = notificationService.getUnreadCount(user);

    assertThat(result).isEqualTo(3L);
  }

  @Test
  void markAsRead_throwsNotFound_whenNotOwned() {
    User user = User.builder().id(1L).build();
    when(notificationRepository.findByIdAndUserId(999L, 1L)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> notificationService.markAsRead(user, 999L))
        .isInstanceOf(CustomException.class)
        .extracting(e -> ((CustomException) e).getErrorCode())
        .isEqualTo(NotificationErrorCode.NOTIFICATION_NOT_FOUND);
  }

  @Test
  void markAsRead_marksNotificationAsRead() {
    User user = User.builder().id(1L).build();
    Notification notification =
        Notification.builder()
            .id(10L)
            .userId(1L)
            .type(NotificationType.HEARING)
            .isRead(false)
            .build();
    when(notificationRepository.findByIdAndUserId(10L, 1L)).thenReturn(Optional.of(notification));
    when(notificationMapper.toResponse(notification))
        .thenReturn(NotificationResponse.builder().id(10L).isRead(true).build());

    notificationService.markAsRead(user, 10L);

    assertThat(notification.getIsRead()).isTrue();
  }

  @Test
  void markAllAsRead_returnsAffectedCount() {
    User user = User.builder().id(1L).build();
    when(notificationRepository.markAllAsRead(1L)).thenReturn(5);

    int result = notificationService.markAllAsRead(user);

    assertThat(result).isEqualTo(5);
  }

  @Test
  void delete_removesNotification() {
    User user = User.builder().id(1L).build();
    Notification notification = Notification.builder().id(20L).userId(1L).build();
    when(notificationRepository.findByIdAndUserId(20L, 1L)).thenReturn(Optional.of(notification));

    notificationService.delete(user, 20L);

    verify(notificationRepository).delete(notification);
  }
}
