/* 
 * Copyright (c) HANIUM SOLOLAW 
 */
package com.hanium.sololaw.domain.schedule.controller;

import java.time.LocalDate;
import java.util.List;

import jakarta.validation.Valid;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.hanium.sololaw.domain.schedule.dto.request.CreateScheduleRequest;
import com.hanium.sololaw.domain.schedule.dto.request.UpdateScheduleRequest;
import com.hanium.sololaw.domain.schedule.dto.response.ScheduleResponse;
import com.hanium.sololaw.domain.schedule.entity.enums.ScheduleType;
import com.hanium.sololaw.domain.schedule.service.ScheduleService;
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
@Tag(name = "Schedule", description = "일정 관련 기능을 제공하는 API")
public class ScheduleController {

  private final ScheduleService scheduleService;

  @Operation(
      summary = "[ 사용자 | 토큰 O | 일정 생성 ]",
      description =
          "caseId(선택), title, eventDate 필수 · eventTime, scheduleType, location, memo, reminderEnabled, reminderValue, reminderUnit 전부 선택")
  @PostMapping("/api/schedules")
  public ResponseEntity<BaseResponse<ScheduleResponse>> create(
      @CurrentUser User user, @Valid @RequestBody CreateScheduleRequest request) {
    ScheduleResponse result = scheduleService.create(user, request);
    return ResponseEntity.ok(BaseResponse.success(result));
  }

  @Operation(
      summary = "[ 사용자 | 토큰 O | 일정 목록/캘린더 조회 ]",
      description =
          "from, to(ISO-8601 날짜), scheduleType, caseId 전부 선택. eventDate 오름차순 정렬, D-day 파생 포함")
  @GetMapping("/api/schedules")
  public ResponseEntity<BaseResponse<List<ScheduleResponse>>> getList(
      @CurrentUser User user,
      @RequestParam(required = false) Long caseId,
      @RequestParam(required = false) ScheduleType scheduleType,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
    List<ScheduleResponse> result = scheduleService.getList(user, caseId, scheduleType, from, to);
    return ResponseEntity.ok(BaseResponse.success(result));
  }

  @Operation(summary = "[ 사용자 | 토큰 O | 일정 상세 조회 ]")
  @GetMapping("/api/schedules/{scheduleId}")
  public ResponseEntity<BaseResponse<ScheduleResponse>> getDetail(
      @CurrentUser User user, @PathVariable Long scheduleId) {
    ScheduleResponse result = scheduleService.getDetail(user, scheduleId);
    return ResponseEntity.ok(BaseResponse.success(result));
  }

  @Operation(summary = "[ 사용자 | 토큰 O | 일정 수정 ]", description = "null인 필드는 변경하지 않음")
  @PatchMapping("/api/schedules/{scheduleId}")
  public ResponseEntity<BaseResponse<ScheduleResponse>> update(
      @CurrentUser User user,
      @PathVariable Long scheduleId,
      @Valid @RequestBody UpdateScheduleRequest request) {
    ScheduleResponse result = scheduleService.update(user, scheduleId, request);
    return ResponseEntity.ok(BaseResponse.success(result));
  }

  @Operation(summary = "[ 사용자 | 토큰 O | 일정 삭제 ]")
  @DeleteMapping("/api/schedules/{scheduleId}")
  public ResponseEntity<BaseResponse<Void>> delete(
      @CurrentUser User user, @PathVariable Long scheduleId) {
    scheduleService.delete(user, scheduleId);
    return ResponseEntity.ok(BaseResponse.success(200, "일정이 삭제되었습니다.", null));
  }
}
