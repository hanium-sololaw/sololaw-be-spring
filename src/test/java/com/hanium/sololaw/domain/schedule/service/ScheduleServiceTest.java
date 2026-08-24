/* 
 * Copyright (c) HANIUM SOLOLAW 
 */
package com.hanium.sololaw.domain.schedule.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.hanium.sololaw.domain.cases.exception.CaseErrorCode;
import com.hanium.sololaw.domain.cases.repository.CaseRepository;
import com.hanium.sololaw.domain.schedule.dto.request.CreateScheduleRequest;
import com.hanium.sololaw.domain.schedule.dto.request.UpdateScheduleRequest;
import com.hanium.sololaw.domain.schedule.dto.response.ScheduleResponse;
import com.hanium.sololaw.domain.schedule.entity.Schedule;
import com.hanium.sololaw.domain.schedule.entity.enums.ReminderUnit;
import com.hanium.sololaw.domain.schedule.entity.enums.ScheduleType;
import com.hanium.sololaw.domain.schedule.exception.ScheduleErrorCode;
import com.hanium.sololaw.domain.schedule.mapper.ScheduleMapper;
import com.hanium.sololaw.domain.schedule.repository.ScheduleRepository;
import com.hanium.sololaw.domain.user.entity.User;
import com.hanium.sololaw.global.exception.CustomException;

@ExtendWith(MockitoExtension.class)
class ScheduleServiceTest {

  @Mock private CaseRepository caseRepository;
  @Mock private ScheduleRepository scheduleRepository;
  @Mock private ScheduleMapper scheduleMapper;

  @InjectMocks private ScheduleServiceImpl scheduleService;

  @Test
  void create_throwsNotFound_whenCaseNotOwned() {
    User user = User.builder().id(1L).build();
    CreateScheduleRequest request =
        new CreateScheduleRequest(
            5L,
            "변론기일",
            ScheduleType.HEARING,
            LocalDate.of(2026, 9, 1),
            null,
            null,
            null,
            null,
            null,
            null);
    when(caseRepository.findByIdAndUserId(5L, 1L)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> scheduleService.create(user, request))
        .isInstanceOf(CustomException.class)
        .extracting(e -> ((CustomException) e).getErrorCode())
        .isEqualTo(CaseErrorCode.CASE_NOT_FOUND);
  }

  @Test
  void create_savesSchedule_whenCaseIdAbsent() {
    User user = User.builder().id(1L).build();
    CreateScheduleRequest request =
        new CreateScheduleRequest(
            null, "개인 일정", null, LocalDate.of(2026, 9, 1), null, null, null, null, null, null);
    Schedule newSchedule = Schedule.builder().userId(1L).title("개인 일정").build();
    Schedule savedSchedule = Schedule.builder().id(10L).userId(1L).title("개인 일정").build();

    when(scheduleMapper.toEntity(1L, request)).thenReturn(newSchedule);
    when(scheduleRepository.save(newSchedule)).thenReturn(savedSchedule);
    when(scheduleMapper.toResponse(savedSchedule))
        .thenReturn(ScheduleResponse.builder().id(10L).build());

    ScheduleResponse result = scheduleService.create(user, request);

    assertThat(result.id()).isEqualTo(10L);
  }

  @Test
  void getList_delegatesToRepositoryWithFilters() {
    User user = User.builder().id(1L).build();
    Schedule schedule = Schedule.builder().id(10L).userId(1L).build();
    LocalDate from = LocalDate.of(2026, 9, 1);
    LocalDate to = LocalDate.of(2026, 9, 30);
    when(scheduleRepository.findAllByUserIdAndFilters(1L, null, ScheduleType.HEARING, from, to))
        .thenReturn(List.of(schedule));
    when(scheduleMapper.toResponseList(List.of(schedule)))
        .thenReturn(List.of(ScheduleResponse.builder().id(10L).build()));

    List<ScheduleResponse> result =
        scheduleService.getList(user, null, ScheduleType.HEARING, from, to);

    assertThat(result).hasSize(1);
  }

  @Test
  void getList_substitutesSentinelDateRange_whenFromAndToAreNull() {
    User user = User.builder().id(1L).build();
    LocalDate minDate = LocalDate.of(1, 1, 1);
    LocalDate maxDate = LocalDate.of(9999, 12, 31);
    when(scheduleRepository.findAllByUserIdAndFilters(1L, null, null, minDate, maxDate))
        .thenReturn(List.of());
    when(scheduleMapper.toResponseList(List.of())).thenReturn(List.of());

    List<ScheduleResponse> result = scheduleService.getList(user, null, null, null, null);

    assertThat(result).isEmpty();
    verify(scheduleRepository).findAllByUserIdAndFilters(1L, null, null, minDate, maxDate);
  }

  @Test
  void getDetail_throwsNotFound_whenNotOwned() {
    User user = User.builder().id(1L).build();
    when(scheduleRepository.findByIdAndUserId(999L, 1L)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> scheduleService.getDetail(user, 999L))
        .isInstanceOf(CustomException.class)
        .extracting(e -> ((CustomException) e).getErrorCode())
        .isEqualTo(ScheduleErrorCode.SCHEDULE_NOT_FOUND);
  }

  @Test
  void update_preservesExistingFields_whenRequestFieldsNull() {
    User user = User.builder().id(1L).build();
    Schedule schedule =
        Schedule.builder()
            .id(20L)
            .userId(1L)
            .title("기존 제목")
            .eventDate(LocalDate.of(2026, 9, 1))
            .reminderEnabled(false)
            .build();
    UpdateScheduleRequest request =
        new UpdateScheduleRequest(null, null, null, null, "서울중앙지방법원", null, null, null, null);
    when(scheduleRepository.findByIdAndUserId(20L, 1L)).thenReturn(Optional.of(schedule));
    when(scheduleMapper.toResponse(schedule))
        .thenReturn(ScheduleResponse.builder().id(20L).build());

    scheduleService.update(user, 20L, request);

    assertThat(schedule.getTitle()).isEqualTo("기존 제목");
    assertThat(schedule.getEventDate()).isEqualTo(LocalDate.of(2026, 9, 1));
    assertThat(schedule.getLocation()).isEqualTo("서울중앙지방법원");
  }

  @Test
  void update_resetsReminderSentAt_soReminderCanFireAgain() {
    User user = User.builder().id(1L).build();
    Schedule schedule =
        Schedule.builder()
            .id(21L)
            .userId(1L)
            .title("변론기일")
            .eventDate(LocalDate.of(2026, 9, 1))
            .reminderEnabled(true)
            .reminderValue(1)
            .reminderUnit(ReminderUnit.DAY)
            .build();
    schedule.markReminderSent();
    UpdateScheduleRequest request =
        new UpdateScheduleRequest(
            null, null, LocalDate.of(2026, 9, 10), null, null, null, null, null, null);
    when(scheduleRepository.findByIdAndUserId(21L, 1L)).thenReturn(Optional.of(schedule));
    when(scheduleMapper.toResponse(schedule))
        .thenReturn(ScheduleResponse.builder().id(21L).build());

    scheduleService.update(user, 21L, request);

    assertThat(schedule.getReminderSentAt()).isNull();
  }

  @Test
  void delete_removesSchedule() {
    User user = User.builder().id(1L).build();
    Schedule schedule = Schedule.builder().id(30L).userId(1L).build();
    when(scheduleRepository.findByIdAndUserId(30L, 1L)).thenReturn(Optional.of(schedule));

    scheduleService.delete(user, 30L);

    verify(scheduleRepository).delete(schedule);
  }
}
