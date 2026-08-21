/* 
 * Copyright (c) HANIUM SOLOLAW 
 */
package com.hanium.sololaw.domain.evidence.service;

import java.util.List;

import com.hanium.sololaw.domain.evidence.dto.request.CreateEvidenceFolderRequest;
import com.hanium.sololaw.domain.evidence.dto.request.UpdateEvidenceFolderRequest;
import com.hanium.sololaw.domain.evidence.dto.response.EvidenceFolderResponse;
import com.hanium.sololaw.domain.user.entity.User;

public interface EvidenceFolderService {

  /**
   * 증거 폴더를 생성합니다. caseId가 있으면 사건 소유자를 검증합니다.
   *
   * @param user : 로그인 사용자
   * @param request : 폴더 생성 요청
   * @return : 생성된 EvidenceFolderResponse
   */
  EvidenceFolderResponse create(User user, CreateEvidenceFolderRequest request);

  /**
   * 로그인 사용자의 증거 폴더 목록을 조회합니다. 폴더별 증거 개수를 함께 집계합니다.
   *
   * @param user : 로그인 사용자
   * @param caseId : 사건 필터(선택)
   * @return : EvidenceFolderResponse 목록
   */
  List<EvidenceFolderResponse> getList(User user, Long caseId);

  /**
   * 증거 폴더를 수정합니다.
   *
   * @param user : 로그인 사용자
   * @param folderId : 수정할 폴더 ID
   * @param request : 폴더 수정 요청
   * @return : 수정된 EvidenceFolderResponse
   */
  EvidenceFolderResponse update(User user, Long folderId, UpdateEvidenceFolderRequest request);

  /**
   * 증거 폴더를 삭제합니다. 소속 증거는 folder_id가 DB SET NULL로 보존되며, S3 파일은 삭제하지 않습니다.
   *
   * @param user : 로그인 사용자
   * @param folderId : 삭제할 폴더 ID
   */
  void delete(User user, Long folderId);
}
