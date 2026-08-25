/* 
 * Copyright (c) HANIUM SOLOLAW 
 */
package com.hanium.sololaw.domain.cases.service;

import org.springframework.stereotype.Service;

import com.hanium.sololaw.domain.cases.dto.request.CalculateLitigationCostRequest;
import com.hanium.sololaw.domain.cases.dto.response.LitigationCostResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class LitigationCostServiceImpl implements LitigationCostService {

  private final LitigationCostCalculator litigationCostCalculator;

  @Override
  public LitigationCostResponse calculate(CalculateLitigationCostRequest request) {
    log.info(
        "[LitigationCostService] calculate() - START | claimAmount: {}, plaintiffCount: {},"
            + " defendantCount: {}, instance: {}",
        request.claimAmount(),
        request.plaintiffCount(),
        request.defendantCount(),
        request.instance());

    /*
       송달료는 원고·피고를 합한 인원수로 계산한다(인지액은 인원수와 무관).
    */
    int partyCount = request.plaintiffCount() + request.defendantCount();

    LitigationCostResponse result =
        litigationCostCalculator.calculate(
            request.claimAmount(), partyCount, request.filingMethod(), request.instance());

    log.info("[LitigationCostService] calculate() - END | totalCost: {}", result.totalCost());
    return result;
  }
}
