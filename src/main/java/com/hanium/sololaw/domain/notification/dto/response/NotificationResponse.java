/* 
 * Copyright (c) HANIUM SOLOLAW 
 */
package com.hanium.sololaw.domain.notification.dto.response;

import java.time.LocalDateTime;

import com.hanium.sololaw.domain.notification.entity.enums.NotificationType;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
@Schema(description = "알림 응답 DTO")
public record NotificationResponse(
    @Schema(description = "알림 ID") Long id,
    @Schema(description = "연관 사건 ID(nullable)") Long relatedCaseId,
    @Schema(description = "알림 유형") NotificationType type,
    @Schema(description = "제목") String title,
    @Schema(description = "내용") String content,
    @Schema(description = "읽음 여부") Boolean isRead,
    @Schema(description = "생성 시각") LocalDateTime createdAt,
    @Schema(description = "수정 시각") LocalDateTime modifiedAt) {}
