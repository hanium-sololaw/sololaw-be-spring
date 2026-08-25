/* 
 * Copyright (c) HANIUM SOLOLAW 
 */
package com.hanium.sololaw.domain.cases.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.hanium.sololaw.domain.cases.dto.request.CreateTodoRequest;
import com.hanium.sololaw.domain.cases.dto.request.UpdateTodoRequest;
import com.hanium.sololaw.domain.cases.entity.ActivityLog;
import com.hanium.sololaw.domain.cases.entity.Case;
import com.hanium.sololaw.domain.cases.entity.CaseTodo;
import com.hanium.sololaw.domain.cases.exception.CaseErrorCode;
import com.hanium.sololaw.domain.cases.mapper.CaseTodoMapper;
import com.hanium.sololaw.domain.cases.repository.ActivityLogRepository;
import com.hanium.sololaw.domain.cases.repository.CaseRepository;
import com.hanium.sololaw.domain.cases.repository.CaseTodoRepository;
import com.hanium.sololaw.domain.user.entity.User;
import com.hanium.sololaw.global.exception.CustomException;

@ExtendWith(MockitoExtension.class)
class CaseTodoServiceTest {

  @Mock private CaseRepository caseRepository;
  @Mock private CaseTodoRepository caseTodoRepository;
  @Mock private ActivityLogRepository activityLogRepository;
  @Mock private CaseTodoMapper caseTodoMapper;

  @InjectMocks private CaseTodoServiceImpl caseTodoService;

  @Test
  void updateTodo_togglesIsDone_whenProvided() {
    User user = User.builder().id(1L).build();
    Case ownedCase = Case.builder().id(5L).userId(1L).build();
    CaseTodo caseTodo =
        CaseTodo.builder().id(30L).caseId(5L).title("증거자료 준비").isDone(false).build();
    UpdateTodoRequest request = new UpdateTodoRequest(null, null, true);

    when(caseRepository.findByIdAndUserId(5L, 1L)).thenReturn(Optional.of(ownedCase));
    when(caseTodoRepository.findByIdAndCaseId(30L, 5L)).thenReturn(Optional.of(caseTodo));

    caseTodoService.updateTodo(user, 5L, 30L, request);

    assertThat(caseTodo.getIsDone()).isTrue();
    assertThat(caseTodo.getTitle()).isEqualTo("증거자료 준비");
  }

  @Test
  void updateTodo_recordsActivity_whenTransitioningToDone() {
    User user = User.builder().id(1L).build();
    Case ownedCase = Case.builder().id(5L).userId(1L).build();
    CaseTodo caseTodo =
        CaseTodo.builder().id(30L).caseId(5L).title("증거자료 준비").isDone(false).build();
    UpdateTodoRequest request = new UpdateTodoRequest(null, null, true);

    when(caseRepository.findByIdAndUserId(5L, 1L)).thenReturn(Optional.of(ownedCase));
    when(caseTodoRepository.findByIdAndCaseId(30L, 5L)).thenReturn(Optional.of(caseTodo));

    caseTodoService.updateTodo(user, 5L, 30L, request);

    ArgumentCaptor<ActivityLog> captor = ArgumentCaptor.forClass(ActivityLog.class);
    verify(activityLogRepository).save(captor.capture());
    assertThat(captor.getValue().getCaseId()).isEqualTo(5L);
    assertThat(captor.getValue().getDescription()).contains("증거자료 준비").contains("완료");
  }

  @Test
  void updateTodo_doesNotRecordActivity_whenAlreadyDone() {
    User user = User.builder().id(1L).build();
    Case ownedCase = Case.builder().id(5L).userId(1L).build();
    CaseTodo caseTodo = CaseTodo.builder().id(30L).caseId(5L).title("증거자료 준비").isDone(true).build();
    UpdateTodoRequest request = new UpdateTodoRequest(null, null, true);

    when(caseRepository.findByIdAndUserId(5L, 1L)).thenReturn(Optional.of(ownedCase));
    when(caseTodoRepository.findByIdAndCaseId(30L, 5L)).thenReturn(Optional.of(caseTodo));

    caseTodoService.updateTodo(user, 5L, 30L, request);

    verify(activityLogRepository, never()).save(any());
  }

  @Test
  void updateTodo_doesNotRecordActivity_whenTogglingOff() {
    User user = User.builder().id(1L).build();
    Case ownedCase = Case.builder().id(5L).userId(1L).build();
    CaseTodo caseTodo = CaseTodo.builder().id(30L).caseId(5L).title("증거자료 준비").isDone(true).build();
    UpdateTodoRequest request = new UpdateTodoRequest(null, null, false);

    when(caseRepository.findByIdAndUserId(5L, 1L)).thenReturn(Optional.of(ownedCase));
    when(caseTodoRepository.findByIdAndCaseId(30L, 5L)).thenReturn(Optional.of(caseTodo));

    caseTodoService.updateTodo(user, 5L, 30L, request);

    assertThat(caseTodo.getIsDone()).isFalse();
    verify(activityLogRepository, never()).save(any());
  }

  @Test
  void addTodo_throwsNotFound_whenCaseNotOwned() {
    User user = User.builder().id(1L).build();
    CreateTodoRequest request = new CreateTodoRequest("증거자료 준비", null);
    when(caseRepository.findByIdAndUserId(999L, 1L)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> caseTodoService.addTodo(user, 999L, request))
        .isInstanceOf(CustomException.class)
        .extracting(e -> ((CustomException) e).getErrorCode())
        .isEqualTo(CaseErrorCode.CASE_NOT_FOUND);
  }

  @Test
  void deleteTodo_throwsNotFound_whenTodoMissing() {
    User user = User.builder().id(1L).build();
    Case ownedCase = Case.builder().id(5L).userId(1L).build();
    when(caseRepository.findByIdAndUserId(5L, 1L)).thenReturn(Optional.of(ownedCase));
    when(caseTodoRepository.findByIdAndCaseId(999L, 5L)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> caseTodoService.deleteTodo(user, 5L, 999L))
        .isInstanceOf(CustomException.class)
        .extracting(e -> ((CustomException) e).getErrorCode())
        .isEqualTo(CaseErrorCode.CASE_TODO_NOT_FOUND);
  }
}
