/* 
 * Copyright (c) HANIUM SOLOLAW 
 */
package com.hanium.sololaw.global.crypto;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@ExtendWith(MockitoExtension.class)
class JsonFieldEncryptorTest {

  private final ObjectMapper objectMapper = new ObjectMapper();

  @Mock private AesEncryptor aesEncryptor;

  @InjectMocks private JsonFieldEncryptor jsonFieldEncryptor;

  @Test
  void encryptField_replacesValue_atTopLevel() {
    JsonNode node =
        objectMapper.readTree("{\"residentId\": \"901010-1234567\", \"name\": \"홍길동\"}");
    when(aesEncryptor.encrypt("901010-1234567")).thenReturn("ENCRYPTED");

    jsonFieldEncryptor.encryptField(node, "residentId");

    assertThat(node.get("residentId").asString()).isEqualTo("ENCRYPTED");
    assertThat(node.get("name").asString()).isEqualTo("홍길동");
  }

  @Test
  void encryptField_findsNestedValues_insideArrayOfObjects() {
    JsonNode node =
        objectMapper.readTree(
            "{\"plaintiffs\": [{\"name\": \"원고1\", \"residentId\": \"111111-1111111\"},"
                + " {\"name\": \"원고2\", \"residentId\": \"222222-2222222\"}]}");
    when(aesEncryptor.encrypt("111111-1111111")).thenReturn("ENC1");
    when(aesEncryptor.encrypt("222222-2222222")).thenReturn("ENC2");

    jsonFieldEncryptor.encryptField(node, "residentId");

    assertThat(node.get("plaintiffs").get(0).get("residentId").asString()).isEqualTo("ENC1");
    assertThat(node.get("plaintiffs").get(1).get("residentId").asString()).isEqualTo("ENC2");
  }

  @Test
  void encryptField_skipsBlankOrMissingValues() {
    JsonNode node = objectMapper.readTree("{\"residentId\": \"\", \"name\": \"홍길동\"}");

    jsonFieldEncryptor.encryptField(node, "residentId");

    assertThat(node.get("residentId").asString()).isEmpty();
    verifyNoInteractions(aesEncryptor);
  }

  @Test
  void decryptField_restoresValue_atNestedDepth() {
    JsonNode node = objectMapper.readTree("{\"defendants\": [{\"residentId\": \"ENCRYPTED\"}]}");
    when(aesEncryptor.decrypt("ENCRYPTED")).thenReturn("901010-1234567");

    jsonFieldEncryptor.decryptField(node, "residentId");

    assertThat(node.get("defendants").get(0).get("residentId").asString())
        .isEqualTo("901010-1234567");
    verify(aesEncryptor).decrypt("ENCRYPTED");
  }
}
