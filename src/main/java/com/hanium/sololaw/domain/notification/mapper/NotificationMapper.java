/* 
 * Copyright (c) HANIUM SOLOLAW 
 */
package com.hanium.sololaw.domain.notification.mapper;

import java.util.List;

import org.springframework.stereotype.Component;

import com.hanium.sololaw.domain.notification.dto.response.NotificationResponse;
import com.hanium.sololaw.domain.notification.entity.Notification;

@Component
public class NotificationMapper {

  /**
   * @param notification : 변환할 Notification Entity
   */
  public NotificationResponse toResponse(Notification notification) {
    return NotificationResponse.builder()
        .id(notification.getId())
        .relatedCaseId(notification.getRelatedCaseId())
        .type(notification.getType())
        .title(notification.getTitle())
        .content(notification.getContent())
        .isRead(notification.getIsRead())
        .createdAt(notification.getCreatedAt())
        .modifiedAt(notification.getModifiedAt())
        .build();
  }

  /**
   * @param notifications : 변환할 Notification Entity 목록
   */
  public List<NotificationResponse> toResponseList(List<Notification> notifications) {
    return notifications.stream().map(this::toResponse).toList();
  }
}
