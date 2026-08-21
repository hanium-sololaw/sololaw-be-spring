/* 
 * Copyright (c) HANIUM SOLOLAW 
 */
package com.hanium.sololaw.domain.schedule.service;

import java.time.LocalDate;
import java.util.List;

import com.hanium.sololaw.domain.schedule.dto.request.CreateScheduleRequest;
import com.hanium.sololaw.domain.schedule.dto.request.UpdateScheduleRequest;
import com.hanium.sololaw.domain.schedule.dto.response.ScheduleResponse;
import com.hanium.sololaw.domain.schedule.entity.enums.ScheduleType;
import com.hanium.sololaw.domain.user.entity.User;

public interface ScheduleService {

  /**
   * 일정을 생성합니다. caseId가 있으면 사건 소유자를 검증합니다.
   *
   * @param user : 로그인 사용자
   * @param request : 일정 생성 요청
   * @return : 생성된 ScheduleResponse
   */
  ScheduleResponse create(User user, CreateScheduleRequest request);

  /**
   * 로그인 사용자의 일정 목록(캘린더)을 조회합니다.
   *
   * @param user : 로그인 사용자
   * @param caseId : 사건 필터(선택)
   * @param scheduleType : 일정 유형 필터(선택)
   * @param from : 조회 시작일(선택)
   * @param to : 조회 종료일(선택)
   * @return : ScheduleResponse 목록(eventDate 오름차순)
   */
  List<ScheduleResponse> getList(
      User user, Long caseId, ScheduleType scheduleType, LocalDate from, LocalDate to);

  /**
   * 일정 상세를 조회합니다.
   *
   * @param user : 로그인 사용자
   * @param scheduleId : 조회할 일정 ID
   * @return : 조회된 ScheduleResponse
   */
  ScheduleResponse getDetail(User user, Long scheduleId);

  /**
   * 일정을 수정합니다.
   *
   * @param user : 로그인 사용자
   * @param scheduleId : 수정할 일정 ID
   * @param request : 일정 수정 요청
   * @return : 수정된 ScheduleResponse
   */
  ScheduleResponse update(User user, Long scheduleId, UpdateScheduleRequest request);

  /**
   * 일정을 삭제합니다.
   *
   * @param user : 로그인 사용자
   * @param scheduleId : 삭제할 일정 ID
   */
  void delete(User user, Long scheduleId);
}
