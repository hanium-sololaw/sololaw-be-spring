/* 
 * Copyright (c) HANIUM SOLOLAW 
 */
package com.hanium.sololaw.domain.precedent.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import com.hanium.sololaw.domain.precedent.dto.request.CreatePrecedentBookmarkRequest;
import com.hanium.sololaw.domain.precedent.dto.response.PrecedentBookmarkResponse;
import com.hanium.sololaw.domain.precedent.entity.PrecedentBookmark;
import com.hanium.sololaw.domain.precedent.entity.enums.LegalCategory;
import com.hanium.sololaw.domain.precedent.exception.PrecedentErrorCode;
import com.hanium.sololaw.domain.precedent.mapper.PrecedentBookmarkMapper;
import com.hanium.sololaw.domain.precedent.repository.PrecedentBookmarkRepository;
import com.hanium.sololaw.domain.user.entity.User;
import com.hanium.sololaw.global.common.OffsetPageResponse;
import com.hanium.sololaw.global.exception.CustomException;

@ExtendWith(MockitoExtension.class)
class PrecedentBookmarkServiceTest {

  @Mock private PrecedentBookmarkRepository precedentBookmarkRepository;
  @Mock private PrecedentBookmarkMapper precedentBookmarkMapper;

  @InjectMocks private PrecedentBookmarkServiceImpl precedentBookmarkService;

  @Test
  void create_savesNewBookmark_whenNotAlreadySaved() {
    User user = User.builder().id(1L).build();
    CreatePrecedentBookmarkRequest request =
        new CreatePrecedentBookmarkRequest(
            "2024다12345",
            "임대차보증금 반환 청구",
            "서울중앙지방법원",
            "2024-01-01",
            LegalCategory.CIVIL,
            null,
            null);
    PrecedentBookmark newBookmark =
        PrecedentBookmark.builder().userId(1L).serialId("2024다12345").build();
    PrecedentBookmark savedBookmark =
        PrecedentBookmark.builder().id(10L).userId(1L).serialId("2024다12345").build();

    when(precedentBookmarkRepository.findByUserIdAndSerialId(1L, "2024다12345"))
        .thenReturn(Optional.empty());
    when(precedentBookmarkMapper.toEntity(1L, request)).thenReturn(newBookmark);
    when(precedentBookmarkRepository.save(newBookmark)).thenReturn(savedBookmark);
    when(precedentBookmarkMapper.toResponse(savedBookmark))
        .thenReturn(PrecedentBookmarkResponse.builder().id(10L).build());

    PrecedentBookmarkResponse result = precedentBookmarkService.create(user, request);

    assertThat(result.id()).isEqualTo(10L);
  }

  @Test
  void create_returnsExistingBookmark_whenAlreadySaved() {
    User user = User.builder().id(1L).build();
    CreatePrecedentBookmarkRequest request =
        new CreatePrecedentBookmarkRequest(
            "2024다12345", "임대차보증금 반환 청구", null, null, null, null, null);
    PrecedentBookmark existingBookmark =
        PrecedentBookmark.builder().id(5L).userId(1L).serialId("2024다12345").build();

    when(precedentBookmarkRepository.findByUserIdAndSerialId(1L, "2024다12345"))
        .thenReturn(Optional.of(existingBookmark));
    when(precedentBookmarkMapper.toResponse(existingBookmark))
        .thenReturn(PrecedentBookmarkResponse.builder().id(5L).build());

    PrecedentBookmarkResponse result = precedentBookmarkService.create(user, request);

    assertThat(result.id()).isEqualTo(5L);
    verify(precedentBookmarkRepository, never()).save(org.mockito.ArgumentMatchers.any());
  }

  @Test
  void getList_returnsPagedResponse() {
    User user = User.builder().id(1L).build();
    PrecedentBookmark bookmark = PrecedentBookmark.builder().id(10L).userId(1L).build();
    PageRequest pageable = PageRequest.of(0, 20);
    Page<PrecedentBookmark> page = new PageImpl<>(java.util.List.of(bookmark), pageable, 1);
    when(precedentBookmarkRepository.findAllByUserId(1L, pageable)).thenReturn(page);
    when(precedentBookmarkMapper.toResponseList(java.util.List.of(bookmark)))
        .thenReturn(java.util.List.of(PrecedentBookmarkResponse.builder().id(10L).build()));

    OffsetPageResponse<PrecedentBookmarkResponse> result =
        precedentBookmarkService.getList(user, pageable);

    assertThat(result.totalElements()).isEqualTo(1);
    assertThat(result.content()).hasSize(1);
  }

  @Test
  void delete_throwsNotFound_whenNotOwned() {
    User user = User.builder().id(1L).build();
    when(precedentBookmarkRepository.findByIdAndUserId(999L, 1L)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> precedentBookmarkService.delete(user, 999L))
        .isInstanceOf(CustomException.class)
        .extracting(e -> ((CustomException) e).getErrorCode())
        .isEqualTo(PrecedentErrorCode.PRECEDENT_BOOKMARK_NOT_FOUND);
  }

  @Test
  void delete_removesBookmark() {
    User user = User.builder().id(1L).build();
    PrecedentBookmark bookmark = PrecedentBookmark.builder().id(10L).userId(1L).build();
    when(precedentBookmarkRepository.findByIdAndUserId(10L, 1L)).thenReturn(Optional.of(bookmark));

    precedentBookmarkService.delete(user, 10L);

    verify(precedentBookmarkRepository).delete(bookmark);
  }
}
