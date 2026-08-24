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

  long countByCaseId(Long caseId);
}
