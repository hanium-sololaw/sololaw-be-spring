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

  @Query(
      "SELECT s FROM Schedule s WHERE s.userId = :userId "
          + "AND (:caseId IS NULL OR s.caseId = :caseId) "
          + "AND (:scheduleType IS NULL OR s.scheduleType = :scheduleType) "
          + "AND (CAST(:from AS date) IS NULL OR s.eventDate >= CAST(:from AS date)) "
          + "AND (CAST(:to AS date) IS NULL OR s.eventDate <= CAST(:to AS date)) "
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
