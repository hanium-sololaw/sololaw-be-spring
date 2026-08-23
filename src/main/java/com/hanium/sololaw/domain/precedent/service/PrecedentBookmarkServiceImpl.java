/* 
 * Copyright (c) HANIUM SOLOLAW 
 */
package com.hanium.sololaw.domain.precedent.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hanium.sololaw.domain.precedent.dto.request.CreatePrecedentBookmarkRequest;
import com.hanium.sololaw.domain.precedent.dto.response.PrecedentBookmarkResponse;
import com.hanium.sololaw.domain.precedent.entity.PrecedentBookmark;
import com.hanium.sololaw.domain.precedent.exception.PrecedentErrorCode;
import com.hanium.sololaw.domain.precedent.mapper.PrecedentBookmarkMapper;
import com.hanium.sololaw.domain.precedent.repository.PrecedentBookmarkRepository;
import com.hanium.sololaw.domain.user.entity.User;
import com.hanium.sololaw.global.common.OffsetPageResponse;
import com.hanium.sololaw.global.exception.CustomException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class PrecedentBookmarkServiceImpl implements PrecedentBookmarkService {

  private final PrecedentBookmarkRepository precedentBookmarkRepository;
  private final PrecedentBookmarkMapper precedentBookmarkMapper;

  @Override
  @Transactional
  public PrecedentBookmarkResponse create(User user, CreatePrecedentBookmarkRequest request) {
    log.info(
        "[PrecedentBookmarkService] create() - START | userId: {}, serialId: {}",
        user.getId(),
        request.serialId());

    /*
       1. 이미 저장된 판례면 기존 행을 그대로 반환(멱등)
    */
    PrecedentBookmark bookmark =
        precedentBookmarkRepository
            .findByUserIdAndSerialId(user.getId(), request.serialId())
            .orElseGet(
                () ->
                    precedentBookmarkRepository.save(
                        precedentBookmarkMapper.toEntity(user.getId(), request)));

    PrecedentBookmarkResponse result = precedentBookmarkMapper.toResponse(bookmark);

    log.info("[PrecedentBookmarkService] create() - END | bookmarkId: {}", bookmark.getId());
    return result;
  }

  @Override
  @Transactional(readOnly = true)
  public OffsetPageResponse<PrecedentBookmarkResponse> getList(User user, Pageable pageable) {
    log.info("[PrecedentBookmarkService] getList() - START | userId: {}", user.getId());

    Page<PrecedentBookmark> pageResult =
        precedentBookmarkRepository.findAllByUserId(user.getId(), pageable);
    OffsetPageResponse<PrecedentBookmarkResponse> result =
        OffsetPageResponse.of(
            precedentBookmarkMapper.toResponseList(pageResult.getContent()),
            pageResult.getTotalElements(),
            pageable.getPageNumber(),
            pageable.getPageSize());

    log.info(
        "[PrecedentBookmarkService] getList() - END | totalElements: {}",
        pageResult.getTotalElements());
    return result;
  }

  @Override
  @Transactional
  public void delete(User user, Long bookmarkId) {
    log.info(
        "[PrecedentBookmarkService] delete() - START | userId: {}, bookmarkId: {}",
        user.getId(),
        bookmarkId);

    PrecedentBookmark bookmark =
        precedentBookmarkRepository
            .findByIdAndUserId(bookmarkId, user.getId())
            .orElseThrow(
                () -> new CustomException(PrecedentErrorCode.PRECEDENT_BOOKMARK_NOT_FOUND));
    precedentBookmarkRepository.delete(bookmark);

    log.info("[PrecedentBookmarkService] delete() - END | bookmarkId: {}", bookmarkId);
  }
}
