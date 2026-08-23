/* 
 * Copyright (c) HANIUM SOLOLAW 
 */
package com.hanium.sololaw.global.crypto;

import java.util.function.UnaryOperator;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ObjectNode;

/**
 * JSON 트리를 재귀 순회하며 지정한 필드명의 문자열 값만 암호화·복호화하는 유틸. 당사자 배열처럼 같은 필드명이 여러 번, 임의 깊이로 등장할 수 있는 payload(예:
 * 소장 payload의 {@code residentId})에서 값만 치환하고 나머지 구조는 그대로 둔다.
 */
@Component
@RequiredArgsConstructor
public class JsonFieldEncryptor {

  private final AesEncryptor aesEncryptor;

  /**
   * @param node : 대상 JSON 트리 루트(null이면 아무 것도 하지 않음)
   * @param fieldName : 암호화할 필드명(예: "residentId")
   */
  public void encryptField(JsonNode node, String fieldName) {
    transformField(node, fieldName, aesEncryptor::encrypt);
  }

  /**
   * @param node : 대상 JSON 트리 루트(null이면 아무 것도 하지 않음)
   * @param fieldName : 복호화할 필드명(예: "residentId")
   */
  public void decryptField(JsonNode node, String fieldName) {
    transformField(node, fieldName, aesEncryptor::decrypt);
  }

  private void transformField(JsonNode node, String fieldName, UnaryOperator<String> transform) {
    if (node == null) {
      return;
    }
    if (node.isObject()) {
      ObjectNode objectNode = (ObjectNode) node;
      JsonNode value = objectNode.get(fieldName);
      if (value != null && value.isString() && !value.asString().isBlank()) {
        objectNode.put(fieldName, transform.apply(value.asString()));
      }
      objectNode
          .properties()
          .forEach(entry -> transformField(entry.getValue(), fieldName, transform));
    } else if (node.isArray()) {
      node.forEach(child -> transformField(child, fieldName, transform));
    }
  }
}
