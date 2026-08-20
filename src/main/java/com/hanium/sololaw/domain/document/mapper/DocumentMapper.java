/* 
 * Copyright (c) HANIUM SOLOLAW 
 */
package com.hanium.sololaw.domain.document.mapper;

import java.util.List;

import org.springframework.stereotype.Component;

import com.hanium.sololaw.domain.document.dto.request.CreateDocumentRequest;
import com.hanium.sololaw.domain.document.dto.response.DocumentDetailResponse;
import com.hanium.sololaw.domain.document.dto.response.DocumentResponse;
import com.hanium.sololaw.domain.document.entity.Document;

@Component
public class DocumentMapper {

  /**
   * @param caseId : 소속 사건 ID
   * @param userId : 작성자 사용자 ID
   * @param request : 변환할 CreateDocumentRequest
   * @return : 변환된 Document Entity
   */
  public Document toEntity(Long caseId, Long userId, CreateDocumentRequest request) {
    return Document.builder()
        .caseId(caseId)
        .userId(userId)
        .docType(request.docType())
        .applicationSubtype(request.applicationSubtype())
        .title(request.title())
        .content(request.content())
        .build();
  }

  /**
   * @param document : 변환할 Document Entity
   */
  public DocumentResponse toResponse(Document document) {
    return DocumentResponse.builder()
        .id(document.getId())
        .caseId(document.getCaseId())
        .docType(document.getDocType())
        .applicationSubtype(document.getApplicationSubtype())
        .title(document.getTitle())
        .status(document.getStatus())
        .isLatest(document.getIsLatest())
        .generatedAt(document.getGeneratedAt())
        .createdAt(document.getCreatedAt())
        .modifiedAt(document.getModifiedAt())
        .build();
  }

  /**
   * @param documents : 변환할 Document Entity 목록
   */
  public List<DocumentResponse> toResponseList(List<Document> documents) {
    return documents.stream().map(this::toResponse).toList();
  }

  /**
   * @param document : 변환할 Document Entity
   */
  public DocumentDetailResponse toDetailResponse(Document document) {
    return DocumentDetailResponse.builder()
        .id(document.getId())
        .caseId(document.getCaseId())
        .docType(document.getDocType())
        .applicationSubtype(document.getApplicationSubtype())
        .title(document.getTitle())
        .status(document.getStatus())
        .isLatest(document.getIsLatest())
        .content(document.getContent())
        .generatedContent(document.getGeneratedContent())
        .generatedText(document.getGeneratedText())
        .generatedAt(document.getGeneratedAt())
        .createdAt(document.getCreatedAt())
        .modifiedAt(document.getModifiedAt())
        .build();
  }
}
