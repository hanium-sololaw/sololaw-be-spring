/* 
 * Copyright (c) HANIUM SOLOLAW 
 */
package com.hanium.sololaw.domain.document.repository;

import java.util.List;
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

  long countByCaseId(Long caseId);

  /** 사건 삭제 시 S3 파일을 먼저 정리하기 위해, DB CASCADE가 지우기 전에 해당 사건의 문서를 모두 가져온다. */
  List<Document> findAllByCaseId(Long caseId);

  /** 회원 탈퇴 시 S3 파일을 먼저 정리하기 위해 쓴다. */
  List<Document> findAllByUserId(Long userId);

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
