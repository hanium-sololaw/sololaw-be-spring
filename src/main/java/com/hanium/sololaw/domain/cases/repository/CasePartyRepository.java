/* 
 * Copyright (c) HANIUM SOLOLAW 
 */
package com.hanium.sololaw.domain.cases.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.hanium.sololaw.domain.cases.entity.CaseParty;

public interface CasePartyRepository extends JpaRepository<CaseParty, Long> {

  List<CaseParty> findAllByCaseId(Long caseId);

  Optional<CaseParty> findByIdAndCaseId(Long id, Long caseId);
}
