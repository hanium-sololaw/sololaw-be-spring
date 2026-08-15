/* 
 * Copyright (c) HANIUM SOLOLAW 
 */
package com.hanium.sololaw.domain.cases.controller;

import java.util.List;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hanium.sololaw.domain.cases.dto.request.CreateTodoRequest;
import com.hanium.sololaw.domain.cases.dto.request.UpdateTodoRequest;
import com.hanium.sololaw.domain.cases.dto.response.CaseTodoResponse;
import com.hanium.sololaw.domain.cases.service.CaseTodoService;
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
@RequestMapping("/api/cases/{caseId}/todos")
@Tag(name = "CaseTodo", description = "사건별 할 일 관련 기능을 제공하는 API")
public class CaseTodoController {

  private final CaseTodoService caseTodoService;

  @Operation(summary = "[ 사용자 | 토큰 O | 할 일 목록 조회 ]")
  @GetMapping
  public ResponseEntity<BaseResponse<List<CaseTodoResponse>>> getTodos(
      @CurrentUser User user, @PathVariable Long caseId) {
    List<CaseTodoResponse> result = caseTodoService.getTodos(user, caseId);
    return ResponseEntity.ok(BaseResponse.success(result));
  }

  @Operation(
      summary = "[ 사용자 | 토큰 O | 할 일 추가 ]",
      description =
          """
            **Parameters**  \n
            title, dueDate(선택) \n
            """)
  @PostMapping
  public ResponseEntity<BaseResponse<CaseTodoResponse>> addTodo(
      @CurrentUser User user,
      @PathVariable Long caseId,
      @Valid @RequestBody CreateTodoRequest request) {
    CaseTodoResponse result = caseTodoService.addTodo(user, caseId, request);
    return ResponseEntity.ok(BaseResponse.success(result));
  }

  @Operation(
      summary = "[ 사용자 | 토큰 O | 할 일 수정/완료 토글 ]",
      description =
          """
            **Parameters**  \n
            title, dueDate, isDone (null인 필드는 변경하지 않음) \n
            """)
  @PatchMapping("/{todoId}")
  public ResponseEntity<BaseResponse<CaseTodoResponse>> updateTodo(
      @CurrentUser User user,
      @PathVariable Long caseId,
      @PathVariable Long todoId,
      @Valid @RequestBody UpdateTodoRequest request) {
    CaseTodoResponse result = caseTodoService.updateTodo(user, caseId, todoId, request);
    return ResponseEntity.ok(BaseResponse.success(result));
  }

  @Operation(summary = "[ 사용자 | 토큰 O | 할 일 삭제 ]")
  @DeleteMapping("/{todoId}")
  public ResponseEntity<BaseResponse<Void>> deleteTodo(
      @CurrentUser User user, @PathVariable Long caseId, @PathVariable Long todoId) {
    caseTodoService.deleteTodo(user, caseId, todoId);
    return ResponseEntity.ok(BaseResponse.success(200, "할 일이 삭제되었습니다.", null));
  }
}
