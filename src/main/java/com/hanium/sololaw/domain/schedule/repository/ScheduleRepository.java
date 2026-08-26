/* 
 * Copyright (c) HANIUM SOLOLAW 
 */
package com.hanium.sololaw.domain.schedule.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.hanium.sololaw.domain.schedule.entity.Schedule;
import com.hanium.sololaw.domain.schedule.entity.enums.ScheduleType;

public interface ScheduleRepository extends JpaRepository<Schedule, Long> {

  Optional<Schedule> findByIdAndUserId(Long id, Long userId);

  /**
   * from·to는 null을 바로 바인딩하지 않는다, CAST(:param AS date) IS NULL 패턴이 Postgres에서 파라미터 타입을 bytea로 잘못 추론해
   * cannot cast type bytea to date 오류로 이어지는 것을 로컬 검증 중 확인했다. 호출부에서 필터가 없을 때 LocalDate.MIN·MAX를 채워
   * 넘긴다.
   */
  @Query(
      "SELECT s FROM Schedule s WHERE s.userId = :userId "
          + "AND (:caseId IS NULL OR s.caseId = :caseId) "
          + "AND (:scheduleType IS NULL OR s.scheduleType = :scheduleType) "
          + "AND s.eventDate >= :from AND s.eventDate <= :to "
          + "ORDER BY s.eventDate ASC")
  List<Schedule> findAllByUserIdAndFilters(
      @Param("userId") Long userId,
      @Param("caseId") Long caseId,
      @Param("scheduleType") ScheduleType scheduleType,
      @Param("from") LocalDate from,
      @Param("to") LocalDate to);

  List<Schedule> findAllByReminderEnabledTrueAndReminderSentAtIsNull();

  /**
   * 오늘 기준 targetDates(예: 7·3·1일 후) 중 하나와 일치하고 오늘자로 아직 발송되지 않은 일정을 찾는다. scheduleType은 걸러내지 않는다 —
   * HEARING·SUBMISSION_DEADLINE로만 좁히면, 화면의 "일정 유형" 드롭다운이 그 두 값과 다른 걸 보낼 때(예: ATTENDANCE) 리마인더가 조용히
   * 안 나가는 문제가 있었다.
   */
  @Query(
      "SELECT s FROM Schedule s WHERE s.eventDate IN (:targetDates) "
          + "AND (s.globalReminderSentDate IS NULL OR s.globalReminderSentDate <> :today)")
  List<Schedule> findAllDueForGlobalReminder(
      @Param("targetDates") List<LocalDate> targetDates, @Param("today") LocalDate today);

  long countByCaseId(Long caseId);
}
