/* 
 * Copyright (c) HANIUM SOLOLAW 
 */
package com.hanium.sololaw.global.common;

import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

@Getter
@Schema(title = "BaseResponse DTO", description = "공통 API 응답 형식")
public class BaseResponse<T> {

  @Schema(description = "요청 성공 여부", example = "true")
  private final boolean success;

  @Schema(description = "HTTP 상태 코드", example = "200")
  private final int code;

  @Schema(description = "도메인별 에러 코드(성공 응답이거나 커스텀 에러가 아니면 null)", example = "SUB4003")
  private final String errorCode;

  @Schema(description = "응답 메시지", example = "요청이 성공적으로 처리되었습니다.")
  private final String message;

  @Schema(description = "응답 시각", example = "2026-06-04T12:00:00")
  private final LocalDateTime timestamp;

  @Schema(description = "응답 데이터")
  private final T data;

  private BaseResponse(boolean success, int code, String errorCode, String message, T data) {
    this.success = success;
    this.code = code;
    this.errorCode = errorCode;
    this.message = message;
    this.timestamp = LocalDateTime.now();
    this.data = data;
  }

  public static <T> BaseResponse<T> success(T data) {
    return new BaseResponse<>(true, 200, null, "요청이 성공적으로 처리되었습니다.", data);
  }

  public static <T> BaseResponse<T> success(String message, T data) {
    return new BaseResponse<>(true, 200, null, message, data);
  }

  public static <T> BaseResponse<T> success(int code, String message, T data) {
    return new BaseResponse<>(true, code, null, message, data);
  }

  public static <T> BaseResponse<T> error(int code, String message) {
    return new BaseResponse<>(false, code, null, message, null);
  }

  public static <T> BaseResponse<T> error(int code, String errorCode, String message) {
    return new BaseResponse<>(false, code, errorCode, message, null);
  }
}
