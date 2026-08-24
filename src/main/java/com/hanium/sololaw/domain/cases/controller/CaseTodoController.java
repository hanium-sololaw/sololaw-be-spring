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
import org.springframework.web.bind.annotation.RequestParam;
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
@Tag(name = "CaseTodo", description = "사건별 할 일 관련 기능을 제공하는 API")
public class CaseTodoController {

  private final CaseTodoService caseTodoService;

  @Operation(
      summary = "[ 사용자 | 토큰 O | 사건별 할 일 목록 조회 ]",
      description =
          """
            **Returns**  \n
            해당 사건에 속한 할 일 목록, dueDate 오름차순(null은 마지막) \n
            """)
  @GetMapping("/api/cases/{caseId}/todos")
  public ResponseEntity<BaseResponse<List<CaseTodoResponse>>> getTodos(
      @CurrentUser User user, @PathVariable Long caseId) {
    List<CaseTodoResponse> result = caseTodoService.getTodos(user, caseId, null);
    return ResponseEntity.ok(BaseResponse.success(result));
  }

  @Operation(
      summary = "[ 사용자 | 토큰 O | 내 할 일 목록 조회(전체 사건) ]",
      description =
          """
            **Parameters**  \n
            caseId(선택), isDone(선택) \n
            \n
            caseId를 생략하면 로그인 사용자 소유 전체 사건의 할 일을 대상으로 조회합니다, 대시보드 완료된 작업 섹션에서 \
            사용합니다. \n
            \n
            **Returns**  \n
            조건에 맞는 할 일 목록, dueDate 오름차순(null은 마지막) \n
            """)
  @GetMapping("/api/todos")
  public ResponseEntity<BaseResponse<List<CaseTodoResponse>>> getAllTodos(
      @CurrentUser User user,
      @RequestParam(required = false) Long caseId,
      @RequestParam(required = false) Boolean isDone) {
    List<CaseTodoResponse> result = caseTodoService.getTodos(user, caseId, isDone);
    return ResponseEntity.ok(BaseResponse.success(result));
  }

  @Operation(
      summary = "[ 사용자 | 토큰 O | 할 일 추가 ]",
      description =
          """
            **Parameters**  \n
            title, dueDate(선택) \n
            \n
            **Returns**  \n
            생성된 할 일
            """)
  @PostMapping("/api/cases/{caseId}/todos")
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
            \n
            **Returns**  \n
            수정된 할 일
            """)
  @PatchMapping("/api/cases/{caseId}/todos/{todoId}")
  public ResponseEntity<BaseResponse<CaseTodoResponse>> updateTodo(
      @CurrentUser User user,
      @PathVariable Long caseId,
      @PathVariable Long todoId,
      @Valid @RequestBody UpdateTodoRequest request) {
    CaseTodoResponse result = caseTodoService.updateTodo(user, caseId, todoId, request);
    return ResponseEntity.ok(BaseResponse.success(result));
  }

  @Operation(summary = "[ 사용자 | 토큰 O | 할 일 삭제 ]")
  @DeleteMapping("/api/cases/{caseId}/todos/{todoId}")
  public ResponseEntity<BaseResponse<Void>> deleteTodo(
      @CurrentUser User user, @PathVariable Long caseId, @PathVariable Long todoId) {
    caseTodoService.deleteTodo(user, caseId, todoId);
    return ResponseEntity.ok(BaseResponse.success(200, "할 일이 삭제되었습니다.", null));
  }
}
