/* 
 * Copyright (c) HANIUM SOLOLAW 
 */
package com.hanium.sololaw.domain.cases.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.hanium.sololaw.domain.cases.entity.Case;
import com.hanium.sololaw.domain.cases.entity.enums.CaseStatus;
import com.hanium.sololaw.domain.cases.entity.enums.CaseType;

public interface CaseRepository extends JpaRepository<Case, Long> {

  Optional<Case> findByIdAndUserId(Long id, Long userId);

  @Query(
      "SELECT c FROM Case c WHERE c.userId = :userId "
          + "AND (:status IS NULL OR c.status = :status) "
          + "AND (:caseType IS NULL OR c.caseType = :caseType)")
  Page<Case> findAllByUserIdAndFilters(
      @Param("userId") Long userId,
      @Param("status") CaseStatus status,
      @Param("caseType") CaseType caseType,
      Pageable pageable);
}
