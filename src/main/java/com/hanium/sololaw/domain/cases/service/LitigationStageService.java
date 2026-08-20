/* 
 * Copyright (c) HANIUM SOLOLAW 
 */
package com.hanium.sololaw.domain.cases.service;

import java.util.List;

import com.hanium.sololaw.domain.cases.dto.request.UpdateStageStatusRequest;
import com.hanium.sololaw.domain.cases.dto.response.LitigationStageResponse;
import com.hanium.sololaw.domain.user.entity.User;

public interface LitigationStageService {

  /**
   * 사건의 절차 타임라인(6단계)을 조회합니다.
   *
   * @param user : 로그인 사용자
   * @param caseId : 소속 사건 ID
   * @return : LitigationStageResponse 목록(stage_order 오름차순)
   */
  List<LitigationStageResponse> getStages(User user, Long caseId);

  /**
   * 절차 단계 상태를 변경하고, 변경 직후 사건의 progress_rate를 완료 단계 비율로 재계산합니다.
   *
   * @param user : 로그인 사용자
   * @param caseId : 소속 사건 ID
   * @param stageId : 상태를 변경할 단계 ID
   * @param request : 단계 상태 변경 요청
   * @return : 수정된 LitigationStageResponse
   */
  LitigationStageResponse updateStageStatus(
      User user, Long caseId, Long stageId, UpdateStageStatusRequest request);
}
