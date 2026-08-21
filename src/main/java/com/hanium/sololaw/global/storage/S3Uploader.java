/* 
 * Copyright (c) HANIUM SOLOLAW 
 */
package com.hanium.sololaw.global.storage;

import java.time.Duration;

import org.springframework.stereotype.Component;

import com.hanium.sololaw.global.config.property.S3Properties;

import lombok.RequiredArgsConstructor;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

/**
 * S3 업로드·presigned URL 발급 공통 유틸. 도메인별로 key prefix(예: {@code documents/}, {@code evidence/})만 다르게
 * 호출해 여러 도메인이 버킷 하나를 공유한다.
 */
@Component
@RequiredArgsConstructor
public class S3Uploader {

  private final S3Client s3Client;
  private final S3Presigner s3Presigner;
  private final S3Properties s3Properties;

  /**
   * 바이트 배열을 S3에 업로드합니다.
   *
   * @param key 저장할 S3 객체 키(예: documents/{documentId}/{uuid}.txt)
   * @param content 업로드할 바이트 배열
   * @param contentType MIME 타입(예: text/plain)
   * @return 업로드된 S3 객체 키(호출부가 그대로 저장해 이후 presigned URL 발급에 재사용)
   */
  public String upload(String key, byte[] content, String contentType) {
    PutObjectRequest request =
        PutObjectRequest.builder()
            .bucket(s3Properties.getBucket())
            .key(key)
            .contentType(contentType)
            .build();
    s3Client.putObject(request, RequestBody.fromBytes(content));
    return key;
  }

  /**
   * 지정한 키의 객체를 다운로드할 수 있는 presigned GET URL을 발급합니다.
   *
   * @param key 대상 S3 객체 키
   * @param expiry URL 유효 기간
   * @return presigned GET URL
   */
  public String generatePresignedGetUrl(String key, Duration expiry) {
    GetObjectRequest getObjectRequest =
        GetObjectRequest.builder().bucket(s3Properties.getBucket()).key(key).build();
    GetObjectPresignRequest presignRequest =
        GetObjectPresignRequest.builder()
            .signatureDuration(expiry)
            .getObjectRequest(getObjectRequest)
            .build();
    PresignedGetObjectRequest presigned = s3Presigner.presignGetObject(presignRequest);
    return presigned.url().toString();
  }

  /**
   * 지정한 키의 객체를 S3에서 삭제합니다.
   *
   * @param key 삭제할 S3 객체 키
   */
  public void deleteObject(String key) {
    DeleteObjectRequest request =
        DeleteObjectRequest.builder().bucket(s3Properties.getBucket()).key(key).build();
    s3Client.deleteObject(request);
  }
}
