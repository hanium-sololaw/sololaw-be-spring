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
import com.hanium.sololaw.domain.cases.entity.CaseTodo;
import com.hanium.sololaw.domain.cases.exception.CaseErrorCode;
import com.hanium.sololaw.domain.cases.mapper.CaseTodoMapper;
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
  private final CaseTodoMapper caseTodoMapper;

  @Override
  @Transactional(readOnly = true)
  public List<CaseTodoResponse> getTodos(User user, Long caseId) {
    log.info("[CaseTodoService] getTodos() - START | userId: {}, caseId: {}", user.getId(), caseId);

    /*
       1. 사건 소유자 검증
    */
    verifyCaseOwnership(caseId, user.getId());

    /*
       2. 할 일 목록 조회 및 ResponseDto Mapping
    */
    List<CaseTodoResponse> result =
        caseTodoMapper.toResponseList(caseTodoRepository.findAllByCaseId(caseId));

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
       - TODO: 08번 activity_logs 도메인 구현 후 완료 시 활동을 기록한다.
    */
    caseTodo.update(
        request.title() != null ? request.title() : caseTodo.getTitle(),
        request.dueDate() != null ? request.dueDate() : caseTodo.getDueDate());
    if (request.isDone() != null) {
      caseTodo.updateIsDone(request.isDone());
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
