/* 
 * Copyright (c) HANIUM SOLOLAW 
 */
package com.hanium.sololaw.domain.cases.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.hanium.sololaw.domain.cases.entity.CaseTodo;

public interface CaseTodoRepository extends JpaRepository<CaseTodo, Long> {

  Optional<CaseTodo> findByIdAndCaseId(Long id, Long caseId);

  /** caseId가 null이면 사용자 소유 전체 사건을 대상으로 조회한다(대시보드 완료된 작업 섹션용). */
  @Query(
      "SELECT t FROM CaseTodo t WHERE t.caseId IN (SELECT c.id FROM Case c WHERE c.userId = :userId) "
          + "AND (:caseId IS NULL OR t.caseId = :caseId) "
          + "AND (:isDone IS NULL OR t.isDone = :isDone) "
          + "ORDER BY t.dueDate ASC NULLS LAST")
  List<CaseTodo> findAllByUserIdAndFilters(
      @Param("userId") Long userId, @Param("caseId") Long caseId, @Param("isDone") Boolean isDone);
}
