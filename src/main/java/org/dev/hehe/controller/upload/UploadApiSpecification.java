package org.dev.hehe.controller.upload;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.dev.hehe.dto.upload.UploadResponse;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

/**
 * 파일 업로드 API Swagger 명세
 */
@Tag(name = "07. Upload", description = "파일 업로드 API")
public interface UploadApiSpecification {

    @Operation(
            summary = "이미지 업로드",
            description = "S3에 이미지를 업로드하고 URL을 반환합니다. (지원 형식: jpg, jpeg, png, webp)"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "업로드 성공",
                    content = @Content(schema = @Schema(implementation = UploadResponse.class))),
            @ApiResponse(responseCode = "400", description = "잘못된 파일 형식 (C002)"),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류 (C001)")
    })
    org.dev.hehe.common.response.ApiResponse<UploadResponse> uploadImage(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "directory", defaultValue = "articles") String directory
    );
}