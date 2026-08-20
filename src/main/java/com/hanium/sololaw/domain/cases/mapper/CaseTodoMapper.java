/* 
 * Copyright (c) HANIUM SOLOLAW 
 */
package com.hanium.sololaw.domain.cases.mapper;

import java.util.List;

import org.springframework.stereotype.Component;

import com.hanium.sololaw.domain.cases.dto.request.CreateTodoRequest;
import com.hanium.sololaw.domain.cases.dto.response.CaseTodoResponse;
import com.hanium.sololaw.domain.cases.entity.CaseTodo;

@Component
public class CaseTodoMapper {

  /**
   * @param caseId : 소속 사건 ID
   * @param request : 변환할 CreateTodoRequest
   * @return : 변환된 CaseTodo Entity
   */
  public CaseTodo toEntity(Long caseId, CreateTodoRequest request) {
    return CaseTodo.builder()
        .caseId(caseId)
        .title(request.title())
        .dueDate(request.dueDate())
        .build();
  }

  /**
   * @param caseTodo : 변환할 CaseTodo Entity
   */
  public CaseTodoResponse toResponse(CaseTodo caseTodo) {
    return CaseTodoResponse.builder()
        .id(caseTodo.getId())
        .title(caseTodo.getTitle())
        .dueDate(caseTodo.getDueDate())
        .isDone(caseTodo.getIsDone())
        .createdAt(caseTodo.getCreatedAt())
        .modifiedAt(caseTodo.getModifiedAt())
        .build();
  }

  /**
   * @param caseTodos : 변환할 CaseTodo Entity 목록
   */
  public List<CaseTodoResponse> toResponseList(List<CaseTodo> caseTodos) {
    return caseTodos.stream().map(this::toResponse).toList();
  }
}
