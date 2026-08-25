/* 
 * Copyright (c) HANIUM SOLOLAW 
 */
package com.hanium.sololaw.domain.cases.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hanium.sololaw.domain.cases.dto.request.CreateTodoRequest;
import com.hanium.sololaw.domain.cases.dto.request.UpdateTodoRequest;
import com.hanium.sololaw.domain.cases.dto.response.CaseTodoResponse;
import com.hanium.sololaw.domain.cases.entity.ActivityLog;
import com.hanium.sololaw.domain.cases.entity.CaseTodo;
import com.hanium.sololaw.domain.cases.exception.CaseErrorCode;
import com.hanium.sololaw.domain.cases.mapper.CaseTodoMapper;
import com.hanium.sololaw.domain.cases.repository.ActivityLogRepository;
import com.hanium.sololaw.domain.cases.repository.CaseRepository;
import com.hanium.sololaw.domain.cases.repository.CaseTodoRepository;
import com.hanium.sololaw.domain.user.entity.User;
import com.hanium.sololaw.global.exception.CustomException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class CaseTodoServiceImpl implements CaseTodoService {

  private final CaseRepository caseRepository;
  private final CaseTodoRepository caseTodoRepository;
  private final ActivityLogRepository activityLogRepository;
  private final CaseTodoMapper caseTodoMapper;

  @Override
  @Transactional(readOnly = true)
  public List<CaseTodoResponse> getTodos(User user, Long caseId, Boolean isDone) {
    log.info(
        "[CaseTodoService] getTodos() - START | userId: {}, caseId: {}, isDone: {}",
        user.getId(),
        caseId,
        isDone);

    /*
       1. 사건 소유자 검증
       - caseId가 없으면(전체 사건 조회) 건너뛴다.
    */
    if (caseId != null) {
      verifyCaseOwnership(caseId, user.getId());
    }

    /*
       2. 할 일 목록 조회 및 ResponseDto Mapping
    */
    List<CaseTodoResponse> result =
        caseTodoMapper.toResponseList(
            caseTodoRepository.findAllByUserIdAndFilters(user.getId(), caseId, isDone));

    log.info("[CaseTodoService] getTodos() - END | caseId: {}, count: {}", caseId, result.size());
    return result;
  }

  @Override
  @Transactional
  public CaseTodoResponse addTodo(User user, Long caseId, CreateTodoRequest request) {
    log.info(
        "[CaseTodoService] addTodo() - START | userId: {}, caseId: {}, title: {}",
        user.getId(),
        caseId,
        request.title());

    /*
       1. 사건 소유자 검증
    */
    verifyCaseOwnership(caseId, user.getId());

    /*
       2. 할 일 생성 및 저장
    */
    CaseTodo savedTodo = caseTodoRepository.save(caseTodoMapper.toEntity(caseId, request));

    /*
       3. ResponseDto Mapping
    */
    CaseTodoResponse result = caseTodoMapper.toResponse(savedTodo);

    log.info("[CaseTodoService] addTodo() - END | todoId: {}", savedTodo.getId());
    return result;
  }

  @Override
  @Transactional
  public CaseTodoResponse updateTodo(
      User user, Long caseId, Long todoId, UpdateTodoRequest request) {
    log.info(
        "[CaseTodoService] updateTodo() - START | userId: {}, caseId: {}, todoId: {}",
        user.getId(),
        caseId,
        todoId);

    /*
       1. 사건 소유자 검증
    */
    verifyCaseOwnership(caseId, user.getId());

    /*
       2. 할 일 조회
    */
    CaseTodo caseTodo =
        caseTodoRepository
            .findByIdAndCaseId(todoId, caseId)
            .orElseThrow(() -> new CustomException(CaseErrorCode.CASE_TODO_NOT_FOUND));

    /*
       3. 내용 수정 및 완료 토글
       - null인 필드는 기존 값을 유지한다.
       - 미완료 -> 완료로 바뀌는 시점에만 활동을 기록한다(이미 완료된 건을 다시 저장하거나 완료 해제할 때는 남기지 않는다).
    */
    caseTodo.update(
        request.title() != null ? request.title() : caseTodo.getTitle(),
        request.dueDate() != null ? request.dueDate() : caseTodo.getDueDate());
    boolean justCompleted = request.isDone() != null && request.isDone() && !caseTodo.getIsDone();
    if (request.isDone() != null) {
      caseTodo.updateIsDone(request.isDone());
    }
    if (justCompleted) {
      activityLogRepository.save(
          ActivityLog.builder()
              .caseId(caseId)
              .description("'%s' 할 일 완료".formatted(caseTodo.getTitle()))
              .build());
    }

    /*
       4. ResponseDto Mapping
    */
    CaseTodoResponse result = caseTodoMapper.toResponse(caseTodo);

    log.info("[CaseTodoService] updateTodo() - END | todoId: {}", todoId);
    return result;
  }

  @Override
  @Transactional
  public void deleteTodo(User user, Long caseId, Long todoId) {
    log.info(
        "[CaseTodoService] deleteTodo() - START | userId: {}, caseId: {}, todoId: {}",
        user.getId(),
        caseId,
        todoId);

    /*
       1. 사건 소유자 검증
    */
    verifyCaseOwnership(caseId, user.getId());

    /*
       2. 할 일 조회 및 삭제
    */
    CaseTodo caseTodo =
        caseTodoRepository
            .findByIdAndCaseId(todoId, caseId)
            .orElseThrow(() -> new CustomException(CaseErrorCode.CASE_TODO_NOT_FOUND));
    caseTodoRepository.delete(caseTodo);

    log.info("[CaseTodoService] deleteTodo() - END | todoId: {}", todoId);
  }

  private void verifyCaseOwnership(Long caseId, Long userId) {
    caseRepository
        .findByIdAndUserId(caseId, userId)
        .orElseThrow(() -> new CustomException(CaseErrorCode.CASE_NOT_FOUND));
  }
}
