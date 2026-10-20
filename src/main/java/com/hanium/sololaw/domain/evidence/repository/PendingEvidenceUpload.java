/* 
 * Copyright (c) HANIUM SOLOLAW 
 */
package com.hanium.sololaw.domain.evidence.repository;

/**
 * createUploadUrl()에서 createEvidence()/replaceFile()까지 이어지는 증거 업로드 대기 세션. 클라이언트가 발급받은 적 없는 임의의 S3
 * 키를 fileUrl로 보내 증거로 등록하는 것을 막기 위해 Redis에 서버 기준값을 둔다.
 */
public record PendingEvidenceUpload(Long userId, Long caseId) {}
