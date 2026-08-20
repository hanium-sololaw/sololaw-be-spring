/* 
 * Copyright (c) HANIUM SOLOLAW 
 */
package com.hanium.sololaw.domain.document.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.hanium.sololaw.domain.document.entity.Document;
import com.hanium.sololaw.domain.document.entity.enums.DocType;
import com.hanium.sololaw.domain.document.entity.enums.DocumentStatus;

public interface DocumentRepository extends JpaRepository<Document, Long> {

  Optional<Document> findByIdAndUserId(Long id, Long userId);

  Optional<Document> findByCaseIdAndDocTypeAndIsLatestTrue(Long caseId, DocType docType);

  @Query(
      "SELECT d FROM Document d WHERE d.userId = :userId "
          + "AND (:caseId IS NULL OR d.caseId = :caseId) "
          + "AND (:docType IS NULL OR d.docType = :docType) "
          + "AND (:status IS NULL OR d.status = :status) "
          + "AND (:isLatest IS NULL OR d.isLatest = :isLatest)")
  Page<Document> findAllByUserIdAndFilters(
      @Param("userId") Long userId,
      @Param("caseId") Long caseId,
      @Param("docType") DocType docType,
      @Param("status") DocumentStatus status,
      @Param("isLatest") Boolean isLatest,
      Pageable pageable);
}
