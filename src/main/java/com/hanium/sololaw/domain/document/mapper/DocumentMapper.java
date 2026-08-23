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
import com.hanium.sololaw.global.crypto.JsonFieldEncryptor;

import lombok.RequiredArgsConstructor;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

/**
 * JSONB 컬럼(content/generatedContent)은 Hibernate의 JSON 포맷 매퍼가 여전히 Jackson 2 기반이라 엔티티에는 {@code
 * String}으로 저장하고, DTO 경계(Spring HTTP 메시지 컨버터가 쓰는 Jackson 3)에서만 {@link JsonNode}로 변환한다.
 */
@Component
@RequiredArgsConstructor
public class DocumentMapper {

  private static final String RESIDENT_ID_FIELD = "residentId";

  private final ObjectMapper objectMapper;
  private final JsonFieldEncryptor jsonFieldEncryptor;

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
        .content(toJson(request.content()))
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
        .content(toJsonNode(document.getContent()))
        .generatedContent(toJsonNode(document.getGeneratedContent()))
        .generatedText(document.getGeneratedText())
        .generatedAt(document.getGeneratedAt())
        .createdAt(document.getCreatedAt())
        .modifiedAt(document.getModifiedAt())
        .build();
  }

  /**
   * DTO의 JsonNode(Jackson 3)를 엔티티 저장용 JSON 문자열로 변환합니다. 트리 안의 {@code residentId} 값은 저장 전 AES로
   * 암호화한다(법상 암호화 저장 대상인 주민등록번호가 소장 등 payload에 평문으로 포함될 수 있음).
   *
   * @param node : 변환할 JsonNode(null이면 null 반환)
   */
  public String toJson(JsonNode node) {
    if (node == null) {
      return null;
    }
    jsonFieldEncryptor.encryptField(node, RESIDENT_ID_FIELD);
    return objectMapper.writeValueAsString(node);
  }

  /**
   * 엔티티에 저장된 JSON 문자열을 DTO 응답용 JsonNode(Jackson 3)로 변환합니다. 트리 안의 {@code residentId} 값은 조회 시 복호화해
   * 돌려준다.
   *
   * @param json : 변환할 JSON 문자열(null이면 null 반환)
   */
  public JsonNode toJsonNode(String json) {
    if (json == null) {
      return null;
    }
    JsonNode node = objectMapper.readTree(json);
    jsonFieldEncryptor.decryptField(node, RESIDENT_ID_FIELD);
    return node;
  }
}
