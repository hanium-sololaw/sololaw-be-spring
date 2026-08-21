/* 
 * Copyright (c) HANIUM SOLOLAW 
 */
package com.hanium.sololaw.domain.schedule.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hanium.sololaw.domain.cases.exception.CaseErrorCode;
import com.hanium.sololaw.domain.cases.repository.CaseRepository;
import com.hanium.sololaw.domain.schedule.dto.request.CreateScheduleRequest;
import com.hanium.sololaw.domain.schedule.dto.request.UpdateScheduleRequest;
import com.hanium.sololaw.domain.schedule.dto.response.ScheduleResponse;
import com.hanium.sololaw.domain.schedule.entity.Schedule;
import com.hanium.sololaw.domain.schedule.entity.enums.ScheduleType;
import com.hanium.sololaw.domain.schedule.exception.ScheduleErrorCode;
import com.hanium.sololaw.domain.schedule.mapper.ScheduleMapper;
import com.hanium.sololaw.domain.schedule.repository.ScheduleRepository;
import com.hanium.sololaw.domain.user.entity.User;
import com.hanium.sololaw.global.exception.CustomException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class ScheduleServiceImpl implements ScheduleService {

  private final CaseRepository caseRepository;
  private final ScheduleRepository scheduleRepository;
  private final ScheduleMapper scheduleMapper;

  @Override
  @Transactional
  public ScheduleResponse create(User user, CreateScheduleRequest request) {
    log.info(
        "[ScheduleService] create() - START | userId: {}, caseId: {}",
        user.getId(),
        request.caseId());

    /*
       1. caseId가 있으면 사건 소유자 검증
    */
    if (request.caseId() != null) {
      caseRepository
          .findByIdAndUserId(request.caseId(), user.getId())
          .orElseThrow(() -> new CustomException(CaseErrorCode.CASE_NOT_FOUND));
    }

    /*
       2. 일정 생성 및 저장
    */
    Schedule savedSchedule =
        scheduleRepository.save(scheduleMapper.toEntity(user.getId(), request));

    /*
       3. ResponseDto Mapping
    */
    ScheduleResponse result = scheduleMapper.toResponse(savedSchedule);

    log.info("[ScheduleService] create() - END | scheduleId: {}", savedSchedule.getId());
    return result;
  }

  @Override
  @Transactional(readOnly = true)
  public List<ScheduleResponse> getList(
      User user, Long caseId, ScheduleType scheduleType, LocalDate from, LocalDate to) {
    log.info(
        "[ScheduleService] getList() - START | userId: {}, caseId: {}, scheduleType: {}, from: {}, to: {}",
        user.getId(),
        caseId,
        scheduleType,
        from,
        to);

    List<Schedule> schedules =
        scheduleRepository.findAllByUserIdAndFilters(user.getId(), caseId, scheduleType, from, to);
    List<ScheduleResponse> result = scheduleMapper.toResponseList(schedules);

    log.info("[ScheduleService] getList() - END | count: {}", result.size());
    return result;
  }

  @Override
  @Transactional(readOnly = true)
  public ScheduleResponse getDetail(User user, Long scheduleId) {
    log.info(
        "[ScheduleService] getDetail() - START | userId: {}, scheduleId: {}",
        user.getId(),
        scheduleId);

    Schedule schedule = findOwnedSchedule(scheduleId, user.getId());
    ScheduleResponse result = scheduleMapper.toResponse(schedule);

    log.info("[ScheduleService] getDetail() - END | scheduleId: {}", scheduleId);
    return result;
  }

  @Override
  @Transactional
  public ScheduleResponse update(User user, Long scheduleId, UpdateScheduleRequest request) {
    log.info(
        "[ScheduleService] update() - START | userId: {}, scheduleId: {}",
        user.getId(),
        scheduleId);

    /*
       1. 일정 조회 및 소유자 검증
    */
    Schedule schedule = findOwnedSchedule(scheduleId, user.getId());

    /*
       2. 일정 수정
       - null인 필드는 기존 값을 유지한다.
    */
    schedule.update(
        request.title() != null ? request.title() : schedule.getTitle(),
        request.scheduleType() != null ? request.scheduleType() : schedule.getScheduleType(),
        request.eventDate() != null ? request.eventDate() : schedule.getEventDate(),
        request.eventTime() != null ? request.eventTime() : schedule.getEventTime(),
        request.location() != null ? request.location() : schedule.getLocation(),
        request.memo() != null ? request.memo() : schedule.getMemo(),
        request.reminderEnabled() != null
            ? request.reminderEnabled()
            : schedule.getReminderEnabled(),
        request.reminderValue() != null ? request.reminderValue() : schedule.getReminderValue(),
        request.reminderUnit() != null ? request.reminderUnit() : schedule.getReminderUnit());

    /*
       3. ResponseDto Mapping
    */
    ScheduleResponse result = scheduleMapper.toResponse(schedule);

    log.info("[ScheduleService] update() - END | scheduleId: {}", scheduleId);
    return result;
  }

  @Override
  @Transactional
  public void delete(User user, Long scheduleId) {
    log.info(
        "[ScheduleService] delete() - START | userId: {}, scheduleId: {}",
        user.getId(),
        scheduleId);

    Schedule schedule = findOwnedSchedule(scheduleId, user.getId());
    scheduleRepository.delete(schedule);

    log.info("[ScheduleService] delete() - END | scheduleId: {}", scheduleId);
  }

  private Schedule findOwnedSchedule(Long scheduleId, Long userId) {
    return scheduleRepository
        .findByIdAndUserId(scheduleId, userId)
        .orElseThrow(() -> new CustomException(ScheduleErrorCode.SCHEDULE_NOT_FOUND));
  }
}
