/* 
 * Copyright (c) HANIUM SOLOLAW 
 */
package com.hanium.sololaw.domain.courtdocument.converter;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Base64;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

class CourtDocumentFileConverterTest {

  private final CourtDocumentFileConverter converter = new CourtDocumentFileConverter();

  @Test
  void toDataUri_encodesBytesWithContentType_forImage() {
    byte[] bytes = {1, 2, 3, 4};
    MockMultipartFile file = new MockMultipartFile("file", "notice.png", "image/png", bytes);

    String dataUri = converter.toDataUri(file);

    assertThat(dataUri)
        .isEqualTo("data:image/png;base64," + Base64.getEncoder().encodeToString(bytes));
  }

  @Test
  void toDataUri_encodesBytesWithContentType_forPdf() {
    byte[] bytes = {5, 6, 7, 8};
    MockMultipartFile file = new MockMultipartFile("file", "notice.pdf", "application/pdf", bytes);

    String dataUri = converter.toDataUri(file);

    assertThat(dataUri)
        .isEqualTo("data:application/pdf;base64," + Base64.getEncoder().encodeToString(bytes));
  }
}
