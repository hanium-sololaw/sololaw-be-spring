/* 
 * Copyright (c) HANIUM SOLOLAW 
 */
package com.hanium.sololaw.domain.notification.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
@Schema(description = "알림 설정 응답 DTO")
public record NotificationSettingResponse(
    @Schema(description = "변론기일 리마인더(기일 3일 전, 1일 전) 알림 여부") Boolean hearingReminderAlert,
    @Schema(description = "제출 기한 알림(준비서면·증거 제출 기한 임박 시) 여부") Boolean submissionDeadlineAlert,
    @Schema(description = "유사 판례 업데이트(새 판례 발견 시) 알림 여부") Boolean aiPrecedentAlert) {}
