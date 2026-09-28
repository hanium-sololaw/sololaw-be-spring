/* 
 * Copyright (c) HANIUM SOLOLAW 
 */
package com.hanium.sololaw.global.ai;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import com.hanium.sololaw.global.config.property.GeminiProperties;
import com.hanium.sololaw.global.exception.CustomException;
import com.hanium.sololaw.global.exception.GlobalErrorCode;

import tools.jackson.databind.json.JsonMapper;

class GeminiVisionClientTest {

  private record TestExtraction(String name, String date) {}

  private MockRestServiceServer mockServer;
  private GeminiVisionClient client;

  @BeforeEach
  void setUp() {
    GeminiProperties properties =
        new GeminiProperties("test-key", "test-model", "https://generativelanguage.googleapis.com");
    RestClient.Builder builder = RestClient.builder();
    mockServer = MockRestServiceServer.bindTo(builder).build();
    client = new GeminiVisionClient(builder, properties, JsonMapper.builder().build());
  }

  @Test
  void extractStructured_parsesNestedJsonTextIntoTargetType() {
    mockServer
        .expect(
            requestTo(
                "https://generativelanguage.googleapis.com/v1beta/models/test-model:generateContent?key=test-key"))
        .andExpect(method(HttpMethod.POST))
        .andRespond(
            withSuccess(
                """
                {
                  "candidates": [
                    {"content": {"parts": [{"text": "{\\"name\\":\\"홍길동\\",\\"date\\":\\"2026-05-31\\"}"}]}}
                  ]
                }
                """,
                MediaType.APPLICATION_JSON));

    TestExtraction result =
        client.extractStructured(
            "system",
            "user",
            "data:image/png;base64,QQ==",
            Map.of("type", "object"),
            TestExtraction.class,
            GlobalErrorCode.INTERNAL_SERVER_ERROR);

    assertThat(result.name()).isEqualTo("홍길동");
    assertThat(result.date()).isEqualTo("2026-05-31");
    mockServer.verify();
  }

  @Test
  void extractStructured_sendsInlineDataWithParsedMimeTypeAndBase64() {
    mockServer
        .expect(requestTo(containsString("generateContent")))
        .andExpect(content().string(containsString("\"mimeType\":\"image/png\"")))
        .andExpect(content().string(containsString("\"data\":\"QQ==\"")))
        .andRespond(
            withSuccess(
                """
                {"candidates": [{"content": {"parts": [{"text": "{\\"name\\":null,\\"date\\":null}"}]}}]}
                """,
                MediaType.APPLICATION_JSON));

    client.extractStructured(
        "system",
        "user",
        "data:image/png;base64,QQ==",
        Map.of("type", "object"),
        TestExtraction.class,
        GlobalErrorCode.INTERNAL_SERVER_ERROR);

    mockServer.verify();
  }

  @Test
  void extractStructured_throwsGivenErrorCode_whenServerErrors() {
    mockServer.expect(requestTo(containsString("generateContent"))).andRespond(withServerError());

    assertThatThrownBy(
            () ->
                client.extractStructured(
                    "system",
                    "user",
                    "data:image/png;base64,QQ==",
                    Map.of("type", "object"),
                    TestExtraction.class,
                    GlobalErrorCode.INTERNAL_SERVER_ERROR))
        .isInstanceOf(CustomException.class)
        .extracting(e -> ((CustomException) e).getErrorCode())
        .isEqualTo(GlobalErrorCode.INTERNAL_SERVER_ERROR);
  }

  @Test
  void extractStructured_throwsGivenErrorCode_whenCandidatesEmpty() {
    mockServer
        .expect(requestTo(containsString("generateContent")))
        .andRespond(withSuccess("{\"candidates\": []}", MediaType.APPLICATION_JSON));

    assertThatThrownBy(
            () ->
                client.extractStructured(
                    "system",
                    "user",
                    "data:image/png;base64,QQ==",
                    Map.of("type", "object"),
                    TestExtraction.class,
                    GlobalErrorCode.INTERNAL_SERVER_ERROR))
        .isInstanceOf(CustomException.class)
        .extracting(e -> ((CustomException) e).getErrorCode())
        .isEqualTo(GlobalErrorCode.INTERNAL_SERVER_ERROR);
  }
}
