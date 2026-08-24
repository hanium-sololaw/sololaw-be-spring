/* 
 * Copyright (c) HANIUM SOLOLAW 
 */
package com.hanium.sololaw.domain.notification.entity;

import jakarta.persistence.*;

import com.hanium.sololaw.global.common.BaseTimeEntity;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "notification_settings")
public class NotificationSetting extends BaseTimeEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "user_id", nullable = false, unique = true)
  private Long userId;

  @Builder.Default
  @Column(nullable = false)
  private Boolean hearingReminderAlert = true;

  @Builder.Default
  @Column(nullable = false)
  private Boolean submissionDeadlineAlert = true;

  @Builder.Default
  @Column(nullable = false)
  private Boolean aiPrecedentAlert = false;

  @Builder.Default
  @Column(nullable = false)
  private Boolean overdueDeadlineAlert = true;

  @Builder.Default
  @Column(nullable = false)
  private Boolean evidenceSupplementAlert = true;

  /**
   * 알림 설정을 부분 수정합니다. 각 파라미터가 null이면 해당 필드는 변경하지 않습니다.
   *
   * @param hearingReminderAlert 변론기일 리마인더(기일 3일 전, 1일 전) 알림 여부
   * @param submissionDeadlineAlert 제출 기한 알림(준비서면·증거 제출 기한 임박 시) 여부
   * @param aiPrecedentAlert 유사 판례 업데이트(새 판례 발견 시) 알림 여부
   * @param overdueDeadlineAlert 기한 지남 알림(기한이 지났는데 완료 표시가 없을 때) 여부
   * @param evidenceSupplementAlert 증거 보완 알림(보완필요로 표시해 둔 증거가 남아 있을 때) 여부
   */
  public void updateSettings(
      Boolean hearingReminderAlert,
      Boolean submissionDeadlineAlert,
      Boolean aiPrecedentAlert,
      Boolean overdueDeadlineAlert,
      Boolean evidenceSupplementAlert) {
    if (hearingReminderAlert != null) this.hearingReminderAlert = hearingReminderAlert;
    if (submissionDeadlineAlert != null) this.submissionDeadlineAlert = submissionDeadlineAlert;
    if (aiPrecedentAlert != null) this.aiPrecedentAlert = aiPrecedentAlert;
    if (overdueDeadlineAlert != null) this.overdueDeadlineAlert = overdueDeadlineAlert;
    if (evidenceSupplementAlert != null) this.evidenceSupplementAlert = evidenceSupplementAlert;
  }

  /**
   * 기본값으로 알림 설정을 생성합니다. userId 외 필드는 이 엔티티의 {@code @Builder.Default} 값을 따릅니다.
   *
   * @param userId 알림 설정을 생성할 사용자 ID
   * @return 기본값으로 구성된 NotificationSetting
   */
  public static NotificationSetting createDefault(Long userId) {
    return NotificationSetting.builder().userId(userId).build();
  }
}
