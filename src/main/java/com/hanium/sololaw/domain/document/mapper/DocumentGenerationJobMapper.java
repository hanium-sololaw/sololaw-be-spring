/* 
 * Copyright (c) HANIUM SOLOLAW 
 */
package com.hanium.sololaw.domain.document.mapper;

import org.springframework.stereotype.Component;

import com.hanium.sololaw.domain.document.dto.response.DocumentGenerationJobResponse;
import com.hanium.sololaw.domain.document.entity.DocumentGenerationJob;

@Component
public class DocumentGenerationJobMapper {

  /**
   * @param job : 변환할 DocumentGenerationJob Entity
   */
  public DocumentGenerationJobResponse toResponse(DocumentGenerationJob job) {
    return DocumentGenerationJobResponse.builder()
        .id(job.getId())
        .documentId(job.getDocumentId())
        .status(job.getStatus())
        .progress(job.getProgress())
        .errorCode(job.getErrorCode())
        .errorMessage(job.getErrorMessage())
        .failedAt(job.getFailedAt())
        .build();
  }
}
