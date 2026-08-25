/* 
 * Copyright (c) HANIUM SOLOLAW 
 */
package com.hanium.sololaw.domain.cases.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.hanium.sololaw.domain.cases.dto.request.CalculateLitigationCostRequest;
import com.hanium.sololaw.domain.cases.dto.response.LitigationCostResponse;
import com.hanium.sololaw.domain.cases.entity.enums.FilingMethod;
import com.hanium.sololaw.domain.cases.entity.enums.LitigationInstance;

@ExtendWith(MockitoExtension.class)
class LitigationCostServiceImplTest {

  @Mock private LitigationCostCalculator litigationCostCalculator;

  @InjectMocks private LitigationCostServiceImpl litigationCostService;

  @Test
  void calculate_sumsPlaintiffAndDefendantCount_beforeDelegatingToCalculator() {
    CalculateLitigationCostRequest request =
        new CalculateLitigationCostRequest(
            BigDecimal.valueOf(5_000_000), 2, 1, FilingMethod.ELECTRONIC, LitigationInstance.FIRST);
    LitigationCostResponse expected =
        new LitigationCostResponse(
            5_000_000, true, true, LitigationInstance.FIRST, 22_500, 3, 22_503, 3, 10, "안내");

    when(litigationCostCalculator.calculate(
            BigDecimal.valueOf(5_000_000), 3, FilingMethod.ELECTRONIC, LitigationInstance.FIRST))
        .thenReturn(expected);

    LitigationCostResponse result = litigationCostService.calculate(request);

    assertThat(result).isEqualTo(expected);
  }
}
