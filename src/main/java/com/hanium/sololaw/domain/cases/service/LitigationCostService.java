/* 
 * Copyright (c) HANIUM SOLOLAW 
 */
package com.hanium.sololaw.domain.cases.service;

import com.hanium.sololaw.domain.cases.dto.request.CalculateLitigationCostRequest;
import com.hanium.sololaw.domain.cases.dto.response.LitigationCostResponse;

public interface LitigationCostService {

  /**
   * 인지대·송달료를 계산합니다. 특정 사건과 무관하게 요청값만으로 계산하는 독립형 계산기입니다.
   *
   * @param request : 청구금액·원고 수·피고 수·제출 방법·심급
   * @return : 산출된 LitigationCostResponse
   */
  LitigationCostResponse calculate(CalculateLitigationCostRequest request);
}
