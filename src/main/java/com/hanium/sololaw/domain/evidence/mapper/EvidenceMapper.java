/* 
 * Copyright (c) HANIUM SOLOLAW 
 */
package com.hanium.sololaw.domain.evidence.mapper;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import org.springframework.stereotype.Component;

import com.hanium.sololaw.domain.evidence.dto.request.CreateEvidenceRequest;
import com.hanium.sololaw.domain.evidence.dto.response.EvidenceResponse;
import com.hanium.sololaw.domain.evidence.entity.Evidence;

@Component
public class EvidenceMapper {

  /**
   * @param caseId : 소속 사건 ID
   * @param request : 변환할 CreateEvidenceRequest
   * @return : 변환된 Evidence Entity
   */
  public Evidence toEntity(Long caseId, CreateEvidenceRequest request) {
    return Evidence.builder()
        .caseId(caseId)
        .folderId(request.folderId())
        .partyType(request.partyType())
        .exhibitNo(request.exhibitNo())
        .fileName(request.fileName())
        .fileUrl(request.fileUrl())
        .fileSize(request.fileSize())
        .fileType(request.fileType())
        .proofPurpose(request.proofPurpose())
        .description(request.description())
        .tags(toArray(request.tags()))
        .deadline(request.deadline())
        .uploadedAt(LocalDateTime.now())
        .build();
  }

  /**
   * @param evidence : 변환할 Evidence Entity
   */
  public EvidenceResponse toResponse(Evidence evidence) {
    return EvidenceResponse.builder()
        .id(evidence.getId())
        .caseId(evidence.getCaseId())
        .folderId(evidence.getFolderId())
        .partyType(evidence.getPartyType())
        .exhibitNo(evidence.getExhibitNo())
        .fileName(evidence.getFileName())
        .fileSize(evidence.getFileSize())
        .fileType(evidence.getFileType())
        .status(evidence.getStatus())
        .proofPurpose(evidence.getProofPurpose())
        .description(evidence.getDescription())
        .tags(toList(evidence.getTags()))
        .submittedAt(evidence.getSubmittedAt())
        .deadline(evidence.getDeadline())
        .uploadedAt(evidence.getUploadedAt())
        .createdAt(evidence.getCreatedAt())
        .modifiedAt(evidence.getModifiedAt())
        .build();
  }

  /**
   * @param evidenceList : 변환할 Evidence Entity 목록
   */
  public List<EvidenceResponse> toResponseList(List<Evidence> evidenceList) {
    return evidenceList.stream().map(this::toResponse).toList();
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
