/* 
 * Copyright (c) HANIUM SOLOLAW 
 */
package com.hanium.sololaw.global.crypto;

import java.nio.ByteBuffer;
import java.security.SecureRandom;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.stereotype.Component;

import com.hanium.sololaw.global.config.property.EncryptionProperties;

import lombok.RequiredArgsConstructor;

/**
 * AES-256-GCM 기반 애플리케이션 레벨 암복호화 유틸. 주민등록번호 등 평문 저장이 금지된 개인정보 컬럼에 사용한다.
 *
 * <p>암호문 포맷: Base64(IV(12byte) + GCM 암호문+태그). 매 호출마다 랜덤 IV를 생성해 앞에 덧붙인다.
 */
@Component
@RequiredArgsConstructor
public class AesEncryptor {

  private static final String ALGORITHM = "AES/GCM/NoPadding";
  private static final int GCM_IV_LENGTH = 12;
  private static final int GCM_TAG_LENGTH = 128;

  private final EncryptionProperties encryptionProperties;
  private final SecureRandom secureRandom = new SecureRandom();

  /**
   * 평문을 AES-256-GCM으로 암호화합니다.
   *
   * @param plainText 암호화할 평문(null이면 null 반환)
   * @return Base64 인코딩된 암호문(IV 포함)
   */
  public String encrypt(String plainText) {
    if (plainText == null) {
      return null;
    }
    try {
      byte[] iv = new byte[GCM_IV_LENGTH];
      secureRandom.nextBytes(iv);

      Cipher cipher = Cipher.getInstance(ALGORITHM);
      cipher.init(Cipher.ENCRYPT_MODE, secretKey(), new GCMParameterSpec(GCM_TAG_LENGTH, iv));
      byte[] cipherBytes =
          cipher.doFinal(plainText.getBytes(java.nio.charset.StandardCharsets.UTF_8));

      ByteBuffer buffer = ByteBuffer.allocate(iv.length + cipherBytes.length);
      buffer.put(iv).put(cipherBytes);
      return Base64.getEncoder().encodeToString(buffer.array());
    } catch (Exception e) {
      throw new IllegalStateException("AES 암호화에 실패했습니다.", e);
    }
  }

  /**
   * {@link #encrypt(String)}로 암호화된 값을 복호화합니다.
   *
   * @param cipherText Base64 인코딩된 암호문(null이면 null 반환)
   * @return 복호화된 평문
   */
  public String decrypt(String cipherText) {
    if (cipherText == null) {
      return null;
    }
    try {
      byte[] decoded = Base64.getDecoder().decode(cipherText);
      ByteBuffer buffer = ByteBuffer.wrap(decoded);
      byte[] iv = new byte[GCM_IV_LENGTH];
      buffer.get(iv);
      byte[] cipherBytes = new byte[buffer.remaining()];
      buffer.get(cipherBytes);

      Cipher cipher = Cipher.getInstance(ALGORITHM);
      cipher.init(Cipher.DECRYPT_MODE, secretKey(), new GCMParameterSpec(GCM_TAG_LENGTH, iv));
      byte[] plainBytes = cipher.doFinal(cipherBytes);
      return new String(plainBytes, java.nio.charset.StandardCharsets.UTF_8);
    } catch (Exception e) {
      throw new IllegalStateException("AES 복호화에 실패했습니다.", e);
    }
  }

  /**
   * 마스킹된 값을 반환합니다(응답 노출용). 원본을 복호화하지 않고 고정 마스크 문자열을 반환한다.
   *
   * @param cipherText 암호화된 값(null이면 null 반환)
   * @return 마스킹된 문자열(예: "990101-1******")
   */
  public String mask(String cipherText) {
    if (cipherText == null) {
      return null;
    }
    String plain = decrypt(cipherText);
    if (plain.length() < 8) {
      return "*".repeat(plain.length());
    }
    return plain.substring(0, 8) + "*".repeat(plain.length() - 8);
  }

  private SecretKeySpec secretKey() {
    byte[] keyBytes = Base64.getDecoder().decode(encryptionProperties.getAesKey());
    return new SecretKeySpec(keyBytes, "AES");
  }
}
