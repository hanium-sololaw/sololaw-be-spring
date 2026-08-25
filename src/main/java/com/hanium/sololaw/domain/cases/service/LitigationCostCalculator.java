/* 
 * Copyright (c) HANIUM SOLOLAW 
 */
package com.hanium.sololaw.domain.cases.service;

import java.math.BigDecimal;

import org.springframework.stereotype.Component;

import com.hanium.sololaw.domain.cases.dto.response.LitigationCostResponse;
import com.hanium.sololaw.domain.cases.entity.enums.FilingMethod;
import com.hanium.sololaw.domain.cases.entity.enums.LitigationInstance;
import com.hanium.sololaw.global.config.property.LitigationCostProperties;

import lombok.RequiredArgsConstructor;

/**
 * 민사소송 인지액·송달료 산출. 민사소송 등 인지법 제2조의 소가 구간별 공식, 제3조의 심급별 배율(항소 1.5배·상고 2배), 전자소송 이용 시 인지액 10% 감경(대법원
 * 공고, '민사소송 등 인지법' 개정)을 반영한다. 법원이 실제 접수 시 다시 계산하는 참고용 수치이며 법적 구속력이 없다.
 */
@Component
@RequiredArgsConstructor
public class LitigationCostCalculator {

  private static final BigDecimal SMALL_CLAIM_THRESHOLD = BigDecimal.valueOf(30_000_000);
  private static final BigDecimal BRACKET_1 = BigDecimal.valueOf(10_000_000);
  private static final BigDecimal BRACKET_2 = BigDecimal.valueOf(100_000_000);
  private static final BigDecimal BRACKET_3 = BigDecimal.valueOf(1_000_000_000);
  private static final BigDecimal MIN_STAMP_FEE = BigDecimal.valueOf(1_000);
  private static final BigDecimal STAMP_ROUND_UNIT = BigDecimal.valueOf(100);
  private static final BigDecimal ELECTRONIC_FILING_DISCOUNT_RATE = BigDecimal.valueOf(0.9);
  private static final BigDecimal ELECTRONIC_ROUND_UNIT = BigDecimal.valueOf(10);
  private static final int SMALL_CLAIM_DELIVERY_COUNT = 10;
  private static final int GENERAL_DELIVERY_COUNT = 15;
  private static final int APPEAL_DELIVERY_COUNT = 12;
  private static final int SUPREME_DELIVERY_COUNT = 8;
  private static final String DISCLAIMER =
      "민사소송 등 인지법 기준 참고용 산출입니다. 실제 접수 금액은 대한민국 법원 전자소송 홈페이지에서 다시 확인하세요.";

  private final LitigationCostProperties properties;

  public LitigationCostResponse calculate(
      BigDecimal claimAmount,
      int partyCount,
      FilingMethod filingMethod,
      LitigationInstance instance) {
    boolean isSmallClaim = claimAmount.compareTo(SMALL_CLAIM_THRESHOLD) <= 0;
    boolean isElectronicFiling = filingMethod == FilingMethod.ELECTRONIC;

    long stampFee = calculateStampFee(claimAmount, instance, isElectronicFiling);

    int deliveryCount = resolveDeliveryCount(instance, isSmallClaim);
    long deliveryFee = (long) partyCount * deliveryCount * properties.getDeliveryFeePerUnit();

    return new LitigationCostResponse(
        claimAmount.longValue(),
        isSmallClaim,
        isElectronicFiling,
        instance,
        stampFee,
        deliveryFee,
        stampFee + deliveryFee,
        partyCount,
        deliveryCount,
        DISCLAIMER);
  }

  private int resolveDeliveryCount(LitigationInstance instance, boolean isSmallClaim) {
    return switch (instance) {
      case FIRST -> isSmallClaim ? SMALL_CLAIM_DELIVERY_COUNT : GENERAL_DELIVERY_COUNT;
      case APPEAL -> APPEAL_DELIVERY_COUNT;
      case SUPREME -> SUPREME_DELIVERY_COUNT;
    };
  }

  private long calculateStampFee(
      BigDecimal claimAmount, LitigationInstance instance, boolean isElectronicFiling) {
    BigDecimal rawFee = applyBracketFormula(claimAmount);
    BigDecimal roundedFee = roundDownTo(rawFee, STAMP_ROUND_UNIT);
    BigDecimal firstInstanceFee =
        roundedFee.compareTo(MIN_STAMP_FEE) < 0 ? MIN_STAMP_FEE : roundedFee;

    BigDecimal stampFee =
        roundDownTo(firstInstanceFee.multiply(instance.getStampFeeMultiplier()), STAMP_ROUND_UNIT);

    if (isElectronicFiling) {
      BigDecimal discounted = stampFee.multiply(ELECTRONIC_FILING_DISCOUNT_RATE);
      stampFee = roundDownTo(discounted, ELECTRONIC_ROUND_UNIT);
    }
    return stampFee.longValueExact();
  }

  private BigDecimal applyBracketFormula(BigDecimal claimAmount) {
    if (claimAmount.compareTo(BRACKET_1) < 0) {
      return claimAmount.multiply(BigDecimal.valueOf(0.005));
    }
    if (claimAmount.compareTo(BRACKET_2) < 0) {
      return claimAmount.multiply(BigDecimal.valueOf(0.0045)).add(BigDecimal.valueOf(5_000));
    }
    if (claimAmount.compareTo(BRACKET_3) < 0) {
      return claimAmount.multiply(BigDecimal.valueOf(0.004)).add(BigDecimal.valueOf(55_000));
    }
    return claimAmount.multiply(BigDecimal.valueOf(0.0035)).add(BigDecimal.valueOf(555_000));
  }

  private BigDecimal roundDownTo(BigDecimal value, BigDecimal unit) {
    return value.divideToIntegralValue(unit).multiply(unit);
  }
}
