package org.dev.hehe.dto.upload;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

/**
 * 파일 업로드 응답 DTO
 */
@Getter
@Builder
@Schema(description = "파일 업로드 응답")
public class UploadResponse {

    @Schema(description = "업로드된 파일의 S3 URL", example = "https://hehe-bucket.s3.ap-northeast-2.amazonaws.com/articles/uuid_image.jpg")
    private final String fileUrl;
}