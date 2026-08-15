/* 
 * Copyright (c) HANIUM SOLOLAW 
 */
package com.hanium.sololaw.domain.cases.mapper;

import java.util.List;

import org.springframework.stereotype.Component;

import com.hanium.sololaw.domain.cases.dto.response.LitigationStageResponse;
import com.hanium.sololaw.domain.cases.entity.LitigationStage;

@Component
public class LitigationStageMapper {

  /**
   * @param litigationStage : 변환할 LitigationStage Entity
   */
  public LitigationStageResponse toResponse(LitigationStage litigationStage) {
    return LitigationStageResponse.builder()
        .id(litigationStage.getId())
        .stageOrder(litigationStage.getStageOrder())
        .name(litigationStage.getName())
        .status(litigationStage.getStatus())
        .stageDate(litigationStage.getStageDate())
        .description(litigationStage.getDescription())
        .build();
  }

  /**
   * @param litigationStages : 변환할 LitigationStage Entity 목록
   */
  public List<LitigationStageResponse> toResponseList(List<LitigationStage> litigationStages) {
    return litigationStages.stream().map(this::toResponse).toList();
  }
}
