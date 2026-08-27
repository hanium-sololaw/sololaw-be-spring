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
import com.hanium.sololaw.domain.cases.entity.enums.LitigationInstance;
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
        calculator.calculate(
            BigDecimal.valueOf(5_000_000), 2, FilingMethod.PAPER, LitigationInstance.FIRST);

    assertThat(result.stampFee()).isEqualTo(25_000);
    assertThat(result.isSmallClaim()).isTrue();
    assertThat(result.deliveryCount()).isEqualTo(10);
    assertThat(result.deliveryFee()).isEqualTo(2L * 10 * DELIVERY_FEE_PER_UNIT);
    assertThat(result.totalCost()).isEqualTo(result.stampFee() + result.deliveryFee());
  }

  @Test
  void calculate_appliesSecondBracket_whenClaimAmountBetween10MillionAnd100Million() {
    LitigationCostResponse result =
        calculator.calculate(
            BigDecimal.valueOf(50_000_000), 2, FilingMethod.PAPER, LitigationInstance.FIRST);

    assertThat(result.stampFee()).isEqualTo(230_000);
    assertThat(result.isSmallClaim()).isFalse();
    assertThat(result.deliveryCount()).isEqualTo(15);
  }

  @Test
  void calculate_appliesThirdBracket_whenClaimAmountBetween100MillionAnd1Billion() {
    LitigationCostResponse result =
        calculator.calculate(
            BigDecimal.valueOf(500_000_000), 2, FilingMethod.PAPER, LitigationInstance.FIRST);

    assertThat(result.stampFee()).isEqualTo(2_055_000);
  }

  @Test
  void calculate_appliesFourthBracket_whenClaimAmountOver1Billion() {
    LitigationCostResponse result =
        calculator.calculate(
            BigDecimal.valueOf(2_000_000_000L), 2, FilingMethod.PAPER, LitigationInstance.FIRST);

    assertThat(result.stampFee()).isEqualTo(7_555_000);
  }

  @Test
  void calculate_roundsDownTo100Won() {
    LitigationCostResponse result =
        calculator.calculate(
            BigDecimal.valueOf(1_234_567), 2, FilingMethod.PAPER, LitigationInstance.FIRST);

    // raw = 1,234,567 * 0.005 = 6,172.835 -> 100원 미만 절사 -> 6,100
    assertThat(result.stampFee()).isEqualTo(6_100);
  }

  @Test
  void calculate_appliesMinimumStampFee_whenCalculatedFeeUnder1000() {
    LitigationCostResponse result =
        calculator.calculate(
            BigDecimal.valueOf(100_000), 2, FilingMethod.PAPER, LitigationInstance.FIRST);

    // raw = 100,000 * 0.005 = 500 -> 최소 인지액 1,000원 적용
    assertThat(result.stampFee()).isEqualTo(1_000);
  }

  @Test
  void calculate_appliesElectronicFilingDiscount() {
    LitigationCostResponse paper =
        calculator.calculate(
            BigDecimal.valueOf(5_000_000), 2, FilingMethod.PAPER, LitigationInstance.FIRST);
    LitigationCostResponse electronic =
        calculator.calculate(
            BigDecimal.valueOf(5_000_000), 2, FilingMethod.ELECTRONIC, LitigationInstance.FIRST);

    assertThat(paper.stampFee()).isEqualTo(25_000);
    assertThat(electronic.isElectronicFiling()).isTrue();
    assertThat(electronic.stampFee()).isEqualTo(22_500); // 25,000 * 0.9
  }

  @Test
  void calculate_treatsExactly30Million_asSmallClaim() {
    LitigationCostResponse result =
        calculator.calculate(
            BigDecimal.valueOf(30_000_000), 2, FilingMethod.PAPER, LitigationInstance.FIRST);

    assertThat(result.isSmallClaim()).isTrue();
    assertThat(result.deliveryCount()).isEqualTo(10);
  }

  @Test
  void calculate_treatsAbove30Million_asGeneralClaim() {
    LitigationCostResponse result =
        calculator.calculate(
            BigDecimal.valueOf(30_000_001), 2, FilingMethod.PAPER, LitigationInstance.FIRST);

    assertThat(result.isSmallClaim()).isFalse();
    assertThat(result.deliveryCount()).isEqualTo(15);
  }

  @Test
  void calculate_multipliesDeliveryFeeByPartyCount() {
    LitigationCostResponse result =
        calculator.calculate(
            BigDecimal.valueOf(5_000_000), 3, FilingMethod.PAPER, LitigationInstance.FIRST);

    assertThat(result.partyCount()).isEqualTo(3);
    assertThat(result.deliveryFee()).isEqualTo(3L * 10 * DELIVERY_FEE_PER_UNIT);
  }

  @Test
  void calculate_appliesAppealMultiplier_1point5x() {
    LitigationCostResponse result =
        calculator.calculate(
            BigDecimal.valueOf(5_000_000), 2, FilingMethod.PAPER, LitigationInstance.APPEAL);

    // 1심 인지액 25,000 * 1.5 = 37,500
    assertThat(result.stampFee()).isEqualTo(37_500);
    assertThat(result.deliveryCount()).isEqualTo(12);
    assertThat(result.deliveryFee()).isEqualTo(2L * 12 * DELIVERY_FEE_PER_UNIT);
  }

  @Test
  void calculate_appliesSupremeMultiplier_2x() {
    LitigationCostResponse result =
        calculator.calculate(
            BigDecimal.valueOf(5_000_000), 2, FilingMethod.PAPER, LitigationInstance.SUPREME);

    // 1심 인지액 25,000 * 2 = 50,000
    assertThat(result.stampFee()).isEqualTo(50_000);
    assertThat(result.deliveryCount()).isEqualTo(8);
    assertThat(result.deliveryFee()).isEqualTo(2L * 8 * DELIVERY_FEE_PER_UNIT);
  }

  @Test
  void calculate_ignoresSmallClaimForAppealDeliveryCount() {
    // 소가 3천만원 이하(소액사건)라도 항소심은 12회 고정 — 1심에서만 소액/일반 구분한다
    LitigationCostResponse result =
        calculator.calculate(
            BigDecimal.valueOf(5_000_000), 2, FilingMethod.PAPER, LitigationInstance.APPEAL);

    assertThat(result.isSmallClaim()).isTrue();
    assertThat(result.deliveryCount()).isEqualTo(12);
  }

  @Test
  void calculate_roundsDownAgainAfterInstanceMultiplier() {
    LitigationCostResponse result =
        calculator.calculate(
            BigDecimal.valueOf(1_234_567), 2, FilingMethod.PAPER, LitigationInstance.APPEAL);

    // 1심 인지액 6,100 * 1.5 = 9,150 -> 100원 미만 절사 -> 9,100
    assertThat(result.stampFee()).isEqualTo(9_100);
  }

  @Test
  void calculate_appliesElectronicDiscountAfterAppealMultiplier() {
    LitigationCostResponse result =
        calculator.calculate(
            BigDecimal.valueOf(5_000_000), 2, FilingMethod.ELECTRONIC, LitigationInstance.APPEAL);

    // 1심 인지액 25,000 * 1.5 = 37,500 -> 전자소송 10% 감경 -> 33,750
    assertThat(result.stampFee()).isEqualTo(33_750);
  }

  @Test
  void calculate_appliesAttorneyFeeMinimum_whenClaimAmountVerySmall() {
    LitigationCostResponse result =
        calculator.calculate(
            BigDecimal.valueOf(1_000_000), 2, FilingMethod.PAPER, LitigationInstance.FIRST);

    // raw = 1,000,000 * 10% = 100,000 -> 최소 30만원 적용
    assertThat(result.attorneyFeeCap()).isEqualTo(300_000);
  }

  @Test
  void calculate_appliesAttorneyFeeFirstBracket_upTo20Million() {
    LitigationCostResponse result =
        calculator.calculate(
            BigDecimal.valueOf(10_000_000), 2, FilingMethod.PAPER, LitigationInstance.FIRST);

    // 10,000,000 * 10% = 1,000,000
    assertThat(result.attorneyFeeCap()).isEqualTo(1_000_000);
  }

  @Test
  void calculate_appliesAttorneyFeeSecondBracket_between20And50Million() {
    LitigationCostResponse result =
        calculator.calculate(
            BigDecimal.valueOf(30_000_000), 2, FilingMethod.PAPER, LitigationInstance.FIRST);

    // 2,000,000 + (30,000,000 - 20,000,000) * 8% = 2,800,000
    assertThat(result.attorneyFeeCap()).isEqualTo(2_800_000);
  }

  @Test
  void calculate_appliesAttorneyFeeIsContinuousAcrossBracketBoundaries() {
    LitigationCostResponse at50m =
        calculator.calculate(
            BigDecimal.valueOf(50_000_000), 2, FilingMethod.PAPER, LitigationInstance.FIRST);
    LitigationCostResponse at100m =
        calculator.calculate(
            BigDecimal.valueOf(100_000_000), 2, FilingMethod.PAPER, LitigationInstance.FIRST);
    LitigationCostResponse at500m =
        calculator.calculate(
            BigDecimal.valueOf(500_000_000), 2, FilingMethod.PAPER, LitigationInstance.FIRST);

    assertThat(at50m.attorneyFeeCap()).isEqualTo(4_400_000);
    assertThat(at100m.attorneyFeeCap()).isEqualTo(7_400_000);
    assertThat(at500m.attorneyFeeCap()).isEqualTo(13_400_000);
  }

  @Test
  void calculate_appliesAttorneyFeeLastBracket_over500Million() {
    LitigationCostResponse result =
        calculator.calculate(
            BigDecimal.valueOf(600_000_000), 2, FilingMethod.PAPER, LitigationInstance.FIRST);

    // 13,400,000 + (600,000,000 - 500,000,000) * 0.5% = 13,900,000
    assertThat(result.attorneyFeeCap()).isEqualTo(13_900_000);
  }

  @Test
  void calculate_excludesAttorneyFeeCapFromTotalCost() {
    LitigationCostResponse result =
        calculator.calculate(
            BigDecimal.valueOf(10_000_000), 2, FilingMethod.PAPER, LitigationInstance.FIRST);

    assertThat(result.totalCost()).isEqualTo(result.stampFee() + result.deliveryFee());
  }
}
