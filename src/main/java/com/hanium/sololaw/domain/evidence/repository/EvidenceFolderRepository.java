/* 
 * Copyright (c) HANIUM SOLOLAW 
 */
package com.hanium.sololaw.domain.evidence.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.hanium.sololaw.domain.evidence.entity.EvidenceFolder;

public interface EvidenceFolderRepository extends JpaRepository<EvidenceFolder, Long> {

  Optional<EvidenceFolder> findByIdAndUserId(Long id, Long userId);

  @Query(
      "SELECT f FROM EvidenceFolder f WHERE f.userId = :userId "
          + "AND (:caseId IS NULL OR f.caseId = :caseId)")
  List<EvidenceFolder> findAllByUserIdAndFilters(
      @Param("userId") Long userId, @Param("caseId") Long caseId);
}
