/* 
 * Copyright (c) HANIUM SOLOLAW 
 */
package com.hanium.sololaw.domain.cases.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.hanium.sololaw.domain.cases.entity.LitigationStage;
import com.hanium.sololaw.domain.cases.entity.enums.StageStatus;

public interface LitigationStageRepository extends JpaRepository<LitigationStage, Long> {

  List<LitigationStage> findAllByCaseIdOrderByStageOrderAsc(Long caseId);

  Optional<LitigationStage> findByIdAndCaseId(Long id, Long caseId);

  boolean existsByCaseId(Long caseId);

  long countByCaseId(Long caseId);

  long countByCaseIdAndStatus(Long caseId, StageStatus status);
}
