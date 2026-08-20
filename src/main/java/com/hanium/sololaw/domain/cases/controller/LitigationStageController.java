/* 
 * Copyright (c) HANIUM SOLOLAW 
 */
package com.hanium.sololaw.domain.cases.controller;

import java.util.List;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hanium.sololaw.domain.cases.dto.request.UpdateStageStatusRequest;
import com.hanium.sololaw.domain.cases.dto.response.LitigationStageResponse;
import com.hanium.sololaw.domain.cases.service.LitigationStageService;
import com.hanium.sololaw.domain.user.entity.User;
import com.hanium.sololaw.global.common.BaseResponse;
import com.hanium.sololaw.global.security.annotation.CurrentUser;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/cases/{caseId}/stages")
@Tag(name = "LitigationStage", description = "사건 절차 단계 관련 기능을 제공하는 API")
public class LitigationStageController {

  private final LitigationStageService litigationStageService;

  @Operation(
      summary = "[ 사용자 | 토큰 O | 절차 타임라인 조회 ]",
      description =
          """
            표준 절차 6단계(분쟁 발생 → 내용증명 → 소장 작성 → 법원 접수 → 변론 → 판결)를 stage_order 오름차순으로 반환합니다.
            """)
  @GetMapping
  public ResponseEntity<BaseResponse<List<LitigationStageResponse>>> getStages(
      @CurrentUser User user, @PathVariable Long caseId) {
    List<LitigationStageResponse> result = litigationStageService.getStages(user, caseId);
    return ResponseEntity.ok(BaseResponse.success(result));
  }

  @Operation(
      summary = "[ 사용자 | 토큰 O | 절차 단계 상태 변경 ]",
      description =
          """
            **Parameters**  \n
            status \n
            \n
            변경 직후 사건의 progress_rate가 완료 단계 비율로 자동 재계산됩니다. 자유 단계 추가/삭제는 제공하지 않습니다.
            """)
  @PatchMapping("/{stageId}/status")
  public ResponseEntity<BaseResponse<LitigationStageResponse>> updateStageStatus(
      @CurrentUser User user,
      @PathVariable Long caseId,
      @PathVariable Long stageId,
      @Valid @RequestBody UpdateStageStatusRequest request) {
    LitigationStageResponse result =
        litigationStageService.updateStageStatus(user, caseId, stageId, request);
    return ResponseEntity.ok(BaseResponse.success(result));
  }
}
