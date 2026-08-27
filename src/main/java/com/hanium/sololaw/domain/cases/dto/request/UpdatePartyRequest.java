/* 
 * Copyright (c) HANIUM SOLOLAW 
 */
package com.hanium.sololaw.domain.cases.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "당사자 상세정보 수정 요청 DTO (null인 필드는 변경하지 않음, 문서 생성 위저드에서 호출)")
public record UpdatePartyRequest(
    @Schema(description = "주민등록번호(평문 전송, 서버가 AES 암호화 저장)", example = "990101-1234567")
        String residentNo,
    @Schema(description = "주소") String address,
    @Schema(description = "연락처") String phone,
    @Schema(description = "이메일") String email,
    @Schema(description = "송달받을 주소(주소와 다를 경우)") String serviceAddress,
    @Schema(description = "팩스") String fax,
    @Schema(description = "법인 대표자·법정대리인") String representative) {}
