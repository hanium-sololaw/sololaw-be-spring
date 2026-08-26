/* 
 * Copyright (c) HANIUM SOLOLAW 
 */
package com.hanium.sololaw.domain.precedent.entity.enums;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

import tools.jackson.databind.ObjectMapper;

class LegalCategoryTest {

  private final ObjectMapper objectMapper = new ObjectMapper();

  @Test
  void from_parsesRagRawKoreanValue() {
    LegalCategory result = objectMapper.readValue("\"민사\"", LegalCategory.class);

    assertThat(result).isEqualTo(LegalCategory.CIVIL);
  }

  @Test
  void from_stillParsesUppercaseConstantName() {
    LegalCategory result = objectMapper.readValue("\"FAMILY\"", LegalCategory.class);

    assertThat(result).isEqualTo(LegalCategory.FAMILY);
  }

  @Test
  void from_rejectsUnknownValue() {
    assertThatThrownBy(() -> objectMapper.readValue("\"상사\"", LegalCategory.class))
        .hasCauseInstanceOf(IllegalArgumentException.class);
  }
}
