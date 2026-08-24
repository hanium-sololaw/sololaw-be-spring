/* 
 * Copyright (c) HANIUM SOLOLAW 
 */
package com.hanium.sololaw.domain.cases.service;

import java.util.List;

import com.hanium.sololaw.domain.cases.dto.request.CreateTodoRequest;
import com.hanium.sololaw.domain.cases.dto.request.UpdateTodoRequest;
import com.hanium.sololaw.domain.cases.dto.response.CaseTodoResponse;
import com.hanium.sololaw.domain.user.entity.User;

public interface CaseTodoService {

  /**
   * 할 일 목록을 조회합니다. 사건 상세 화면 "지금 해야 할 일" 카드에서는 caseId를 필수로 넘기고, caseId가 없으면 사용자 소유 전체 사건을 대상으로
   * 조회합니다(대시보드 완료된 작업 섹션용).
   *
   * @param user : 로그인 사용자
   * @param caseId : 소속 사건 ID(선택, 없으면 전체 사건 대상)
   * @param isDone : 완료 여부 필터(선택)
   * @return : CaseTodoResponse 목록
   */
  List<CaseTodoResponse> getTodos(User user, Long caseId, Boolean isDone);

  /**
   * 할 일을 추가합니다.
   *
   * @param user : 로그인 사용자
   * @param caseId : 소속 사건 ID
   * @param request : 할 일 추가 요청
   * @return : 생성된 CaseTodoResponse
   */
  CaseTodoResponse addTodo(User user, Long caseId, CreateTodoRequest request);

  /**
   * 할 일을 수정하거나 완료 토글합니다. activity_logs(08번) 미구현으로 완료 시 활동 기록은 남기지 않습니다.
   *
   * @param user : 로그인 사용자
   * @param caseId : 소속 사건 ID
   * @param todoId : 수정할 할 일 ID
   * @param request : 할 일 수정 요청
   * @return : 수정된 CaseTodoResponse
   */
  CaseTodoResponse updateTodo(User user, Long caseId, Long todoId, UpdateTodoRequest request);

  /**
   * 할 일을 삭제합니다.
   *
   * @param user : 로그인 사용자
   * @param caseId : 소속 사건 ID
   * @param todoId : 삭제할 할 일 ID
   */
  void deleteTodo(User user, Long caseId, Long todoId);
}
