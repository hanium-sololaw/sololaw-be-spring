/* 
 * Copyright (c) HANIUM SOLOLAW 
 */
package com.hanium.sololaw.domain.document.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.hanium.sololaw.domain.document.entity.DocumentGenerationJob;
import com.hanium.sololaw.domain.document.entity.enums.JobStatus;

public interface DocumentGenerationJobRepository
    extends JpaRepository<DocumentGenerationJob, Long> {

  Optional<DocumentGenerationJob> findByIdAndUserId(Long id, Long userId);

  Optional<DocumentGenerationJob> findByDocumentIdAndStatus(Long documentId, JobStatus status);

  Optional<DocumentGenerationJob> findFirstByDocumentIdOrderByCreatedAtDesc(Long documentId);
}
