/* 
 * Copyright (c) HANIUM SOLOLAW 
 */
package com.hanium.sololaw.domain.evidence.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.hanium.sololaw.domain.evidence.entity.Evidence;
import com.hanium.sololaw.domain.evidence.entity.enums.EvidenceStatus;
import com.hanium.sololaw.domain.evidence.entity.enums.ExhibitParty;

public interface EvidenceRepository extends JpaRepository<Evidence, Long> {

  /** evidence는 user_id 컬럼이 없어(소속 사건을 통해서만 소유자를 알 수 있음) cases와 서브쿼리로 조인해 소유자를 검증한다. */
  @Query(
      "SELECT e FROM Evidence e WHERE e.id = :evidenceId "
          + "AND e.caseId IN (SELECT c.id FROM Case c WHERE c.userId = :userId)")
  Optional<Evidence> findByIdAndUserId(
      @Param("evidenceId") Long evidenceId, @Param("userId") Long userId);

  /** caseId가 null이면 사용자 소유 전체 사건을 대상으로 조회한다(전체 사건 증빙자료 화면용). */
  @Query(
      "SELECT e FROM Evidence e WHERE e.caseId IN (SELECT c.id FROM Case c WHERE c.userId = :userId) "
          + "AND (:caseId IS NULL OR e.caseId = :caseId) "
          + "AND (:status IS NULL OR e.status = :status) "
          + "AND (:partyType IS NULL OR e.partyType = :partyType) "
          + "AND (:folderId IS NULL OR e.folderId = :folderId) "
          + "AND (:isLatest IS NULL OR e.isLatest = :isLatest)")
  Page<Evidence> findAllByUserIdAndFilters(
      @Param("userId") Long userId,
      @Param("caseId") Long caseId,
      @Param("status") EvidenceStatus status,
      @Param("partyType") ExhibitParty partyType,
      @Param("folderId") Long folderId,
      @Param("isLatest") Boolean isLatest,
      Pageable pageable);

  long countByFolderId(Long folderId);

  long countByCaseIdAndPartyType(Long caseId, ExhibitParty partyType);

  long countByCaseId(Long caseId);

  List<Evidence> findAllByStatusAndSupplementAlertSentAtIsNull(EvidenceStatus status);
}
