/* 
 * Copyright (c) HANIUM SOLOLAW 
 */
package com.hanium.sololaw.domain.cases.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.hanium.sololaw.domain.cases.entity.ActivityLog;

public interface ActivityLogRepository extends JpaRepository<ActivityLog, Long> {

  long countByCaseId(Long caseId);

  List<ActivityLog> findTop5ByCaseIdOrderByCreatedAtDesc(Long caseId);
}
