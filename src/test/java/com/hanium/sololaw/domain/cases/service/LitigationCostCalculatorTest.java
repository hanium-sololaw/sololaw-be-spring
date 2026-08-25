/* 
 * Copyright (c) HANIUM SOLOLAW 
 */
package com.hanium.sololaw.domain.cases.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.hanium.sololaw.domain.cases.dto.response.LitigationCostResponse;
import com.hanium.sololaw.domain.cases.entity.enums.FilingMethod;
import com.hanium.sololaw.global.config.property.LitigationCostProperties;

class LitigationCostCalculatorTest {

  private static final long DELIVERY_FEE_PER_UNIT = 5640;

  private LitigationCostCalculator calculator;

  @BeforeEach
  void setUp() {
    calculator = new LitigationCostCalculator(new LitigationCostProperties(DELIVERY_FEE_PER_UNIT));
  }

  @Test
  void calculate_appliesFirstBracket_whenClaimAmountUnder10Million() {
    LitigationCostResponse result =
        calculator.calculate(BigDecimal.valueOf(5_000_000), 2, FilingMethod.PAPER);

    assertThat(result.stampFee()).isEqualTo(25_000);
    assertThat(result.isSmallClaim()).isTrue();
    assertThat(result.deliveryCount()).isEqualTo(10);
    assertThat(result.deliveryFee()).isEqualTo(2L * 10 * DELIVERY_FEE_PER_UNIT);
    assertThat(result.totalCost()).isEqualTo(result.stampFee() + result.deliveryFee());
  }

  @Test
  void calculate_appliesSecondBracket_whenClaimAmountBetween10MillionAnd100Million() {
    LitigationCostResponse result =
        calculator.calculate(BigDecimal.valueOf(50_000_000), 2, FilingMethod.PAPER);

    assertThat(result.stampFee()).isEqualTo(230_000);
    assertThat(result.isSmallClaim()).isFalse();
    assertThat(result.deliveryCount()).isEqualTo(15);
  }

  @Test
  void calculate_appliesThirdBracket_whenClaimAmountBetween100MillionAnd1Billion() {
    LitigationCostResponse result =
        calculator.calculate(BigDecimal.valueOf(500_000_000), 2, FilingMethod.PAPER);

    assertThat(result.stampFee()).isEqualTo(2_055_000);
  }

  @Test
  void calculate_appliesFourthBracket_whenClaimAmountOver1Billion() {
    LitigationCostResponse result =
        calculator.calculate(BigDecimal.valueOf(2_000_000_000L), 2, FilingMethod.PAPER);

    assertThat(result.stampFee()).isEqualTo(7_555_000);
  }

  @Test
  void calculate_roundsDownTo100Won() {
    LitigationCostResponse result =
        calculator.calculate(BigDecimal.valueOf(1_234_567), 2, FilingMethod.PAPER);

    // raw = 1,234,567 * 0.005 = 6,172.835 -> 100원 미만 절사 -> 6,100
    assertThat(result.stampFee()).isEqualTo(6_100);
  }

  @Test
  void calculate_appliesMinimumStampFee_whenCalculatedFeeUnder1000() {
    LitigationCostResponse result =
        calculator.calculate(BigDecimal.valueOf(100_000), 2, FilingMethod.PAPER);

    // raw = 100,000 * 0.005 = 500 -> 최소 인지액 1,000원 적용
    assertThat(result.stampFee()).isEqualTo(1_000);
  }

  @Test
  void calculate_appliesElectronicFilingDiscount() {
    LitigationCostResponse paper =
        calculator.calculate(BigDecimal.valueOf(5_000_000), 2, FilingMethod.PAPER);
    LitigationCostResponse electronic =
        calculator.calculate(BigDecimal.valueOf(5_000_000), 2, FilingMethod.ELECTRONIC);

    assertThat(paper.stampFee()).isEqualTo(25_000);
    assertThat(electronic.isElectronicFiling()).isTrue();
    assertThat(electronic.stampFee()).isEqualTo(22_500); // 25,000 * 0.9
  }

  @Test
  void calculate_treatsExactly30Million_asSmallClaim() {
    LitigationCostResponse result =
        calculator.calculate(BigDecimal.valueOf(30_000_000), 2, FilingMethod.PAPER);

    assertThat(result.isSmallClaim()).isTrue();
    assertThat(result.deliveryCount()).isEqualTo(10);
  }

  @Test
  void calculate_treatsAbove30Million_asGeneralClaim() {
    LitigationCostResponse result =
        calculator.calculate(BigDecimal.valueOf(30_000_001), 2, FilingMethod.PAPER);

    assertThat(result.isSmallClaim()).isFalse();
    assertThat(result.deliveryCount()).isEqualTo(15);
  }

  @Test
  void calculate_multipliesDeliveryFeeByPartyCount() {
    LitigationCostResponse result =
        calculator.calculate(BigDecimal.valueOf(5_000_000), 3, FilingMethod.PAPER);

    assertThat(result.partyCount()).isEqualTo(3);
    assertThat(result.deliveryFee()).isEqualTo(3L * 10 * DELIVERY_FEE_PER_UNIT);
  }
}
