/* 
 * Copyright (c) HANIUM SOLOLAW 
 */
package com.hanium.sololaw.domain.precedent.mapper;

import java.util.List;

import org.springframework.stereotype.Component;

import com.hanium.sololaw.domain.precedent.dto.request.CreatePrecedentBookmarkRequest;
import com.hanium.sololaw.domain.precedent.dto.response.PrecedentBookmarkResponse;
import com.hanium.sololaw.domain.precedent.entity.PrecedentBookmark;

@Component
public class PrecedentBookmarkMapper {

  /**
   * @param userId : 소유자 사용자 ID
   * @param request : 변환할 CreatePrecedentBookmarkRequest
   * @return : 변환된 PrecedentBookmark Entity
   */
  public PrecedentBookmark toEntity(Long userId, CreatePrecedentBookmarkRequest request) {
    return PrecedentBookmark.builder()
        .userId(userId)
        .serialId(request.serialId())
        .name(request.name())
        .caseNo(request.caseNo())
        .court(request.court())
        .decisionDate(request.decisionDate())
        .outcome(request.outcome())
        .category(request.category())
        .referenceNote(request.referenceNote())
        .detailUrl(request.detailUrl())
        .build();
  }

  /**
   * @param bookmark : 변환할 PrecedentBookmark Entity
   */
  public PrecedentBookmarkResponse toResponse(PrecedentBookmark bookmark) {
    return PrecedentBookmarkResponse.builder()
        .id(bookmark.getId())
        .serialId(bookmark.getSerialId())
        .name(bookmark.getName())
        .caseNo(bookmark.getCaseNo())
        .court(bookmark.getCourt())
        .decisionDate(bookmark.getDecisionDate())
        .outcome(bookmark.getOutcome())
        .category(bookmark.getCategory())
        .referenceNote(bookmark.getReferenceNote())
        .detailUrl(bookmark.getDetailUrl())
        .createdAt(bookmark.getCreatedAt())
        .modifiedAt(bookmark.getModifiedAt())
        .build();
  }

  /**
   * @param bookmarks : 변환할 PrecedentBookmark Entity 목록
   */
  public List<PrecedentBookmarkResponse> toResponseList(List<PrecedentBookmark> bookmarks) {
    return bookmarks.stream().map(this::toResponse).toList();
  }
}
