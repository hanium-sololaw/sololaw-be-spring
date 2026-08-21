/* 
 * Copyright (c) HANIUM SOLOLAW 
 */
package com.hanium.sololaw.domain.notification.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.hanium.sololaw.domain.notification.entity.Notification;
import com.hanium.sololaw.domain.notification.entity.enums.NotificationType;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

  Optional<Notification> findByIdAndUserId(Long id, Long userId);

  @Query(
      "SELECT n FROM Notification n WHERE n.userId = :userId "
          + "AND (:isRead IS NULL OR n.isRead = :isRead) "
          + "AND (:type IS NULL OR n.type = :type)")
  Page<Notification> findAllByUserIdAndFilters(
      @Param("userId") Long userId,
      @Param("isRead") Boolean isRead,
      @Param("type") NotificationType type,
      Pageable pageable);

  long countByUserIdAndIsReadFalse(Long userId);

  @Modifying(clearAutomatically = true)
  @Query("UPDATE Notification n SET n.isRead = true WHERE n.userId = :userId AND n.isRead = false")
  int markAllAsRead(@Param("userId") Long userId);
}
