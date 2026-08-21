/* 
 * Copyright (c) HANIUM SOLOLAW 
 */
package com.hanium.sololaw.domain.evidence.mapper;

import java.util.Arrays;
import java.util.List;

import org.springframework.stereotype.Component;

import com.hanium.sololaw.domain.evidence.dto.request.CreateEvidenceFolderRequest;
import com.hanium.sololaw.domain.evidence.dto.response.EvidenceFolderResponse;
import com.hanium.sololaw.domain.evidence.entity.EvidenceFolder;

@Component
public class EvidenceFolderMapper {

  /**
   * @param userId : 소유자 사용자 ID
   * @param request : 변환할 CreateEvidenceFolderRequest
   * @return : 변환된 EvidenceFolder Entity
   */
  public EvidenceFolder toEntity(Long userId, CreateEvidenceFolderRequest request) {
    return EvidenceFolder.builder()
        .userId(userId)
        .caseId(request.caseId())
        .name(request.name())
        .folderType(request.folderType())
        .tags(toArray(request.tags()))
        .build();
  }

  /**
   * @param folder : 변환할 EvidenceFolder Entity
   * @param evidenceCount : 폴더 내 증거 개수(별도 집계)
   */
  public EvidenceFolderResponse toResponse(EvidenceFolder folder, long evidenceCount) {
    return EvidenceFolderResponse.builder()
        .id(folder.getId())
        .caseId(folder.getCaseId())
        .name(folder.getName())
        .folderType(folder.getFolderType())
        .tags(toList(folder.getTags()))
        .evidenceCount(evidenceCount)
        .createdAt(folder.getCreatedAt())
        .modifiedAt(folder.getModifiedAt())
        .build();
  }

  /**
   * @param tags : 변환할 태그 목록(null이면 null 반환)
   */
  public String[] toArray(List<String> tags) {
    return tags != null ? tags.toArray(new String[0]) : null;
  }

  /**
   * @param tags : 변환할 태그 배열(null이면 null 반환)
   */
  public List<String> toList(String[] tags) {
    return tags != null ? Arrays.asList(tags) : null;
  }
}
