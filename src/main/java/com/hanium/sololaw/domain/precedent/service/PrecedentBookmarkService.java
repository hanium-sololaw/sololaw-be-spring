/* 
 * Copyright (c) HANIUM SOLOLAW 
 */
package com.hanium.sololaw.domain.precedent.service;

import org.springframework.data.domain.Pageable;

import com.hanium.sololaw.domain.precedent.dto.request.CreatePrecedentBookmarkRequest;
import com.hanium.sololaw.domain.precedent.dto.response.PrecedentBookmarkResponse;
import com.hanium.sololaw.domain.user.entity.User;
import com.hanium.sololaw.global.common.OffsetPageResponse;

public interface PrecedentBookmarkService {

  /**
   * 판례를 저장합니다. 이미 저장된(user_id, serial_id) 판례면 기존 행을 그대로 반환합니다(멱등).
   *
   * @param user : 로그인 사용자
   * @param request : 판례 저장 요청
   * @return : 저장된(또는 기존) PrecedentBookmarkResponse
   */
  PrecedentBookmarkResponse create(User user, CreatePrecedentBookmarkRequest request);

  /**
   * 로그인 사용자가 저장한 판례 목록을 조회합니다.
   *
   * @param user : 로그인 사용자
   * @param pageable : page(0-base)/size/sort 쿼리 파라미터 바인딩
   * @return : OffsetPageResponse<PrecedentBookmarkResponse>
   */
  OffsetPageResponse<PrecedentBookmarkResponse> getList(User user, Pageable pageable);

  /**
   * 판례 저장을 해제합니다.
   *
   * @param user : 로그인 사용자
   * @param bookmarkId : 해제할 저장 ID
   */
  void delete(User user, Long bookmarkId);
}
