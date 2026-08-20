/* 
 * Copyright (c) HANIUM SOLOLAW 
 */
package com.hanium.sololaw.domain.cases.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.hanium.sololaw.domain.cases.entity.CaseTodo;

public interface CaseTodoRepository extends JpaRepository<CaseTodo, Long> {

  List<CaseTodo> findAllByCaseId(Long caseId);

  Optional<CaseTodo> findByIdAndCaseId(Long id, Long caseId);
}
