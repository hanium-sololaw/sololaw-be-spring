/* 
 * Copyright (c) HANIUM SOLOLAW 
 */
package com.hanium.sololaw.global.common;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 공통 오프셋 페이징 응답(spring-api-spec.md "공통 페이징 규약"). {@code page}는 0-base다.
 *
 * @param content 현재 페이지의 항목 목록
 * @param page 현재 페이지 번호(0-base)
 * @param size 페이지 크기
 * @param totalElements 전체 항목 수
 * @param totalPages 전체 페이지 수
 * @param hasNext 다음 페이지 존재 여부
 */
@Schema(description = "공통 오프셋 페이징 응답")
public record OffsetPageResponse<T>(
    List<T> content, int page, int size, long totalElements, int totalPages, boolean hasNext) {

  /**
   * 조회 결과로부터 페이징 응답을 생성합니다.
   *
   * @param content 현재 페이지의 항목 목록
   * @param totalElements 전체 항목 수
   * @param page 현재 페이지 번호(0-base)
   * @param size 페이지 크기
   * @return 구성된 OffsetPageResponse
   */
  public static <T> OffsetPageResponse<T> of(
      List<T> content, long totalElements, int page, int size) {
    int totalPages = size == 0 ? 0 : (int) Math.ceil((double) totalElements / size);
    boolean hasNext = (long) (page + 1) * size < totalElements;
    return new OffsetPageResponse<>(content, page, size, totalElements, totalPages, hasNext);
  }
}
