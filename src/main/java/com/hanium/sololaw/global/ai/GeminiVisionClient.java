/* 
 * Copyright (c) HANIUM SOLOLAW 
 */
package com.hanium.sololaw.global.ai;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import com.hanium.sololaw.global.config.property.GeminiProperties;
import com.hanium.sololaw.global.exception.CustomException;
import com.hanium.sololaw.global.exception.model.BaseErrorCode;

import lombok.extern.slf4j.Slf4j;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

/**
 * Gemini Vision(구조화 출력)으로 이미지·PDF에서 정보를 추출하는 범용 클라이언트.
 *
 * <p>특정 도메인에 종속되지 않는다 — 시스템 프롬프트·응답 JSON Schema·역직렬화 대상 타입·실패 시 던질 에러코드를 호출부가 전부 넘겨받는 구조라, 기일통지서 분석
 * 외에 다른 AI 추출 기능이 생기더라도 이 클라이언트를 그대로 재사용할 수 있다. {@code TossPaymentGateway}와 동일하게 {@link
 * RestClient.Builder}를 받아 직접 baseUrl을 설정한다.
 */
@Slf4j
@Component
public class GeminiVisionClient {

  private final RestClient restClient;
  private final GeminiProperties properties;
  private final ObjectMapper objectMapper;

  public GeminiVisionClient(
      RestClient.Builder restClientBuilder,
      GeminiProperties properties,
      ObjectMapper objectMapper) {
    this.properties = properties;
    this.objectMapper = objectMapper;
    // 공유 빈일 수 있는 builder를 그대로 변경하면 다른 소비자에게 영향을 주므로 clone 후 설정한다.
    this.restClient = restClientBuilder.clone().baseUrl(properties.getBaseUrl()).build();
  }

  /**
   * 파일(이미지 또는 PDF)과 프롬프트를 Gemini에 보내 JSON Schema에 맞는 구조화된 결과를 받는다.
   *
   * @param systemPrompt 시스템 지시문
   * @param userPrompt 사용자 프롬프트(텍스트)
   * @param fileDataUri {@code data:<mime>;base64,<...>} 형태의 파일 — Gemini는 PDF도 이미지와 동일한
   *     방식(inlineData)으로 그대로 받을 수 있어 별도 렌더링이 필요 없다
   * @param responseSchema JSON Schema(표준 형식 — nullable은 {@code type: [".., "null"]} 배열로 표현)
   * @param targetType 역직렬화할 대상 타입
   * @param failureErrorCode 호출 실패·응답 파싱 실패 시 던질 도메인별 에러코드(호출부가 결정)
   * @return {@code targetType}으로 역직렬화된 추출 결과
   */
  public <T> T extractStructured(
      String systemPrompt,
      String userPrompt,
      String fileDataUri,
      Map<String, Object> responseSchema,
      Class<T> targetType,
      BaseErrorCode failureErrorCode) {
    log.info(
        "[GeminiVisionClient] extractStructured() - START | targetType: {}",
        targetType.getSimpleName());

    DataUri file = DataUri.parse(fileDataUri);

    Map<String, Object> body =
        Map.of(
            "systemInstruction",
            Map.of("parts", List.of(Map.of("text", systemPrompt))),
            "contents",
            List.of(
                Map.of(
                    "role",
                    "user",
                    "parts",
                    List.of(
                        Map.of("text", userPrompt),
                        Map.of(
                            "inlineData",
                            Map.of("mimeType", file.mimeType(), "data", file.base64Data()))))),
            "generationConfig",
            Map.of("responseMimeType", "application/json", "responseSchema", responseSchema));

    JsonNode response;
    try {
      response =
          restClient
              .post()
              .uri(
                  "/v1beta/models/{model}:generateContent?key={key}",
                  properties.getModel(),
                  properties.getApiKey())
              .body(body)
              .retrieve()
              .body(JsonNode.class);
    } catch (RestClientException e) {
      log.error("[GeminiVisionClient] extractStructured() - FAIL | error: {}", e.getMessage());
      throw new CustomException(failureErrorCode);
    }

    // path(...)는 구조가 어긋나도 항상 MissingNode를 반환해 asString()이 안전하게 ""로 떨어진다.
    String content =
        response == null
            ? ""
            : response
                .path("candidates")
                .path(0)
                .path("content")
                .path("parts")
                .path(0)
                .path("text")
                .asString();
    if (content.isBlank()) {
      log.error("[GeminiVisionClient] extractStructured() - FAIL | empty content");
      throw new CustomException(failureErrorCode);
    }

    try {
      T result = objectMapper.readValue(content, targetType);
      log.info("[GeminiVisionClient] extractStructured() - END");
      return result;
    } catch (Exception e) {
      log.error(
          "[GeminiVisionClient] extractStructured() - FAIL | parse error: {}", e.getMessage());
      throw new CustomException(failureErrorCode);
    }
  }

  /** {@code data:<mime>;base64,<data>} 형태의 문자열을 mime 타입과 base64 본문으로 분리한다. */
  private record DataUri(String mimeType, String base64Data) {
    private static DataUri parse(String dataUri) {
      int mimeStart = "data:".length();
      int mimeEnd = dataUri.indexOf(";base64,");
      String mimeType = dataUri.substring(mimeStart, mimeEnd);
      String base64Data = dataUri.substring(mimeEnd + ";base64,".length());
      return new DataUri(mimeType, base64Data);
    }
  }
}
