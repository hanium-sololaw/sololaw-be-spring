/* 
 * Copyright (c) HANIUM SOLOLAW 
 */
package com.hanium.sololaw.domain.evidence.service;

import org.springframework.data.domain.Pageable;

import com.hanium.sololaw.domain.evidence.dto.request.CreateEvidenceRequest;
import com.hanium.sololaw.domain.evidence.dto.request.CreateEvidenceUploadUrlRequest;
import com.hanium.sololaw.domain.evidence.dto.request.UpdateEvidenceRequest;
import com.hanium.sololaw.domain.evidence.dto.request.UpdateEvidenceStatusRequest;
import com.hanium.sololaw.domain.evidence.dto.response.EvidenceResponse;
import com.hanium.sololaw.domain.evidence.dto.response.EvidenceUploadUrlResponse;
import com.hanium.sololaw.domain.evidence.entity.enums.EvidenceStatus;
import com.hanium.sololaw.domain.evidence.entity.enums.ExhibitParty;
import com.hanium.sololaw.domain.user.entity.User;
import com.hanium.sololaw.global.common.OffsetPageResponse;

public interface EvidenceService {

  /**
   * 증거 업로드용 presigned PUT URL을 발급합니다. 확정 반영 전 용량을 사전 확인합니다(원자 예약은 등록 시점에 수행).
   *
   * @param user : 로그인 사용자
   * @param request : 업로드 URL 발급 요청(caseId 포함)
   * @return : 발급된 EvidenceUploadUrlResponse
   */
  EvidenceUploadUrlResponse createUploadUrl(User user, CreateEvidenceUploadUrlRequest request);

  /**
   * 증거를 등록합니다(presigned URL 업로드 완료 후 메타 확정). 저장 용량을 원자적으로 예약하며 초과 시 413을 반환합니다.
   *
   * @param user : 로그인 사용자
   * @param caseId : 소속 사건 ID
   * @param request : 증거 등록 요청
   * @return : 등록된 EvidenceResponse
   */
  EvidenceResponse createEvidence(User user, Long caseId, CreateEvidenceRequest request);

  /**
   * 사건별 증거 목록을 조회합니다.
   *
   * @param user : 로그인 사용자
   * @param caseId : 소속 사건 ID
   * @param status : 제출 상태 필터(선택)
   * @param partyType : 호증 당사자 필터(선택)
   * @param folderId : 폴더 필터(선택)
   * @param pageable : page(0-base)/size/sort 쿼리 파라미터 바인딩
   * @return : OffsetPageResponse<EvidenceResponse>
   */
  OffsetPageResponse<EvidenceResponse> getList(
      User user,
      Long caseId,
      EvidenceStatus status,
      ExhibitParty partyType,
      Long folderId,
      Pageable pageable);

  /**
   * 증거 상세를 조회합니다.
   *
   * @param user : 로그인 사용자
   * @param evidenceId : 조회할 증거 ID
   * @return : 조회된 EvidenceResponse
   */
  EvidenceResponse getDetail(User user, Long evidenceId);

  /**
   * 증거 메타데이터를 수정합니다(파일 자체는 교체 불가).
   *
   * @param user : 로그인 사용자
   * @param evidenceId : 수정할 증거 ID
   * @param request : 증거 수정 요청
   * @return : 수정된 EvidenceResponse
   */
  EvidenceResponse update(User user, Long evidenceId, UpdateEvidenceRequest request);

  /**
   * 증거 제출 상태를 변경합니다. SUBMITTED로 전이 시 submittedAt을 채웁니다.
   *
   * @param user : 로그인 사용자
   * @param evidenceId : 상태를 변경할 증거 ID
   * @param request : 상태 변경 요청
   * @return : 수정된 EvidenceResponse
   */
  EvidenceResponse updateStatus(User user, Long evidenceId, UpdateEvidenceStatusRequest request);

  /**
   * 증거 다운로드용 presigned URL을 발급합니다.
   *
   * @param user : 로그인 사용자
   * @param evidenceId : 다운로드할 증거 ID
   * @return : presigned GET URL
   */
  String getDownloadUrl(User user, Long evidenceId);

  /**
   * 증거를 삭제합니다. 저장 용량을 반환하고 S3 실 파일도 함께 삭제합니다.
   *
   * @param user : 로그인 사용자
   * @param evidenceId : 삭제할 증거 ID
   */
  void delete(User user, Long evidenceId);
}
