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
 * 민사소송 인지액·송달료·변호사보수 인정액 산출. 민사소송 등 인지법 제2조의 소가 구간별 공식, 제3조의 심급별 배율(항소 1.5배·상고 2배), 전자소송 이용 시 인지액
 * 10% 감경(대법원 공고, '민사소송 등 인지법' 개정)을 반영한다. 변호사보수 인정액은 변호사보수의 소송비용 산입에 관한 규칙 제3조·별표를 반영한다. 법원이 실제
 * 접수·확정 시 다시 계산하는 참고용 수치이며 법적 구속력이 없다.
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

  /*
     변호사보수의 소송비용 산입에 관한 규칙 제3조·별표(2020.12.28. 시행) 기준.
     각 심급단위로 소가에 별표 기준을 그대로 적용하며(심급별 배율 없음), 산정액이 30만원 미만이면 30만원으로 한다.
     실제 소송비용액 확정은 법원이 하며, 소송의 특성에 따라 법원이 1/2 한도에서 증액할 수 있으므로 참고용 수치다.
  */
  private static final BigDecimal ATTORNEY_FEE_MIN = BigDecimal.valueOf(300_000);
  private static final BigDecimal ATTORNEY_FEE_BRACKET_1 = BigDecimal.valueOf(20_000_000);
  private static final BigDecimal ATTORNEY_FEE_BRACKET_2 = BigDecimal.valueOf(50_000_000);
  private static final BigDecimal ATTORNEY_FEE_BRACKET_3 = BigDecimal.valueOf(100_000_000);
  private static final BigDecimal ATTORNEY_FEE_BRACKET_4 = BigDecimal.valueOf(150_000_000);
  private static final BigDecimal ATTORNEY_FEE_BRACKET_5 = BigDecimal.valueOf(200_000_000);
  private static final BigDecimal ATTORNEY_FEE_BRACKET_6 = BigDecimal.valueOf(500_000_000);

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

    long attorneyFeeCap = calculateAttorneyFeeCap(claimAmount);

    return new LitigationCostResponse(
        claimAmount.longValue(),
        isSmallClaim,
        isElectronicFiling,
        instance,
        stampFee,
        deliveryFee,
        stampFee + deliveryFee,
        attorneyFeeCap,
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

  private long calculateAttorneyFeeCap(BigDecimal claimAmount) {
    BigDecimal rawFee = applyAttorneyFeeBracketFormula(claimAmount);
    BigDecimal fee = rawFee.compareTo(ATTORNEY_FEE_MIN) < 0 ? ATTORNEY_FEE_MIN : rawFee;
    return roundDownTo(fee, BigDecimal.ONE).longValueExact();
  }

  private BigDecimal applyAttorneyFeeBracketFormula(BigDecimal claimAmount) {
    if (claimAmount.compareTo(ATTORNEY_FEE_BRACKET_1) <= 0) {
      return claimAmount.multiply(BigDecimal.valueOf(0.1));
    }
    if (claimAmount.compareTo(ATTORNEY_FEE_BRACKET_2) <= 0) {
      return claimAmount
          .subtract(ATTORNEY_FEE_BRACKET_1)
          .multiply(BigDecimal.valueOf(0.08))
          .add(BigDecimal.valueOf(2_000_000));
    }
    if (claimAmount.compareTo(ATTORNEY_FEE_BRACKET_3) <= 0) {
      return claimAmount
          .subtract(ATTORNEY_FEE_BRACKET_2)
          .multiply(BigDecimal.valueOf(0.06))
          .add(BigDecimal.valueOf(4_400_000));
    }
    if (claimAmount.compareTo(ATTORNEY_FEE_BRACKET_4) <= 0) {
      return claimAmount
          .subtract(ATTORNEY_FEE_BRACKET_3)
          .multiply(BigDecimal.valueOf(0.04))
          .add(BigDecimal.valueOf(7_400_000));
    }
    if (claimAmount.compareTo(ATTORNEY_FEE_BRACKET_5) <= 0) {
      return claimAmount
          .subtract(ATTORNEY_FEE_BRACKET_4)
          .multiply(BigDecimal.valueOf(0.02))
          .add(BigDecimal.valueOf(9_400_000));
    }
    if (claimAmount.compareTo(ATTORNEY_FEE_BRACKET_6) <= 0) {
      return claimAmount
          .subtract(ATTORNEY_FEE_BRACKET_5)
          .multiply(BigDecimal.valueOf(0.01))
          .add(BigDecimal.valueOf(10_400_000));
    }
    return claimAmount
        .subtract(ATTORNEY_FEE_BRACKET_6)
        .multiply(BigDecimal.valueOf(0.005))
        .add(BigDecimal.valueOf(13_400_000));
  }
}
