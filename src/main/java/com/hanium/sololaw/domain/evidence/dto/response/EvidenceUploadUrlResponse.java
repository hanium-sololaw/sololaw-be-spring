/* 
 * Copyright (c) HANIUM SOLOLAW 
 */
package com.hanium.sololaw.domain.evidence.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
@Schema(description = "증거 업로드용 presigned URL 발급 응답 DTO")
public record EvidenceUploadUrlResponse(
    @Schema(description = "프론트가 PUT으로 직접 업로드할 presigned URL") String uploadUrl,
    @Schema(description = "업로드될 S3 객체 키(이후 증거 등록 요청에 그대로 사용)") String key) {}
