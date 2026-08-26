/* 
 * Copyright (c) HANIUM SOLOLAW 
 */
package com.hanium.sololaw.domain.precedent.entity.enums;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

import tools.jackson.databind.ObjectMapper;

class PrecedentOutcomeTest {

  private final ObjectMapper objectMapper = new ObjectMapper();

  @Test
  void from_parsesRagLowercaseValue() {
    PrecedentOutcome result = objectMapper.readValue("\"win\"", PrecedentOutcome.class);

    assertThat(result).isEqualTo(PrecedentOutcome.WIN);
  }

  @Test
  void from_stillParsesUppercaseConstantName() {
    PrecedentOutcome result = objectMapper.readValue("\"PARTIAL\"", PrecedentOutcome.class);

    assertThat(result).isEqualTo(PrecedentOutcome.PARTIAL);
  }

  @Test
  void from_rejectsUnknownValue() {
    assertThatThrownBy(() -> objectMapper.readValue("\"draw\"", PrecedentOutcome.class))
        .hasCauseInstanceOf(IllegalArgumentException.class);
  }
}
