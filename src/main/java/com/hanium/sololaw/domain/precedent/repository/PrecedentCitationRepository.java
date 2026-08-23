/* 
 * Copyright (c) HANIUM SOLOLAW 
 */
package com.hanium.sololaw.domain.precedent.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.hanium.sololaw.domain.precedent.entity.PrecedentCitation;

public interface PrecedentCitationRepository extends JpaRepository<PrecedentCitation, Long> {

  Optional<PrecedentCitation> findByIdAndUserId(Long id, Long userId);

  @Query(
      "SELECT c FROM PrecedentCitation c WHERE c.userId = :userId "
          + "AND (:caseId IS NULL OR c.caseId = :caseId) "
          + "AND (:documentId IS NULL OR c.documentId = :documentId) "
          + "ORDER BY c.citedAt DESC")
  List<PrecedentCitation> findAllByUserIdAndFilters(
      @Param("userId") Long userId,
      @Param("caseId") Long caseId,
      @Param("documentId") Long documentId);
}
