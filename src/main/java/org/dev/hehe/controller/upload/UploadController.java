package org.dev.hehe.controller.upload;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dev.hehe.common.exception.CommonException;
import org.dev.hehe.common.exception.ErrorCode;
import org.dev.hehe.common.response.ApiResult;
import org.dev.hehe.dto.upload.UploadResponse;
import org.dev.hehe.service.s3.S3Service;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 파일 업로드 컨트롤러
 * Swagger 명세는 UploadApiSpecification 인터페이스 참고
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/upload")
public class UploadController implements UploadApiSpecification {

    private static final List<String> ALLOWED_CONTENT_TYPES = List.of(
            "image/jpeg", "image/jpg", "image/png", "image/webp"
    );
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB

    private final S3Service s3Service;

    @Override
    @PostMapping(value = "/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResult<UploadResponse> uploadImage(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "directory", defaultValue = "articles") String directory
    ) {
        log.info("[POST] /api/v1/upload/image - 이미지 업로드 요청 - directory: {}, fileName: {}, size: {}bytes",
                directory, file.getOriginalFilename(), file.getSize());

        validateFile(file);

        String fileUrl = s3Service.upload(file, directory);
        return ApiResult.ok(UploadResponse.builder().fileUrl(fileUrl).build());
    }

    /**
     * 파일 유효성 검사
     * - 파일 크기: 최대 5MB
     * - 파일 형식: jpg, jpeg, png, webp만 허용
     *
     * @param file 검사할 파일
     * @throws BusinessException 유효하지 않은 파일인 경우 INVALID_INPUT
     */
    private void validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new CommonException(ErrorCode.INVALID_INPUT);
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            log.warn("파일 크기 초과 - size: {}bytes", file.getSize());
            throw new CommonException(ErrorCode.INVALID_INPUT);
        }

        if (!ALLOWED_CONTENT_TYPES.contains(file.getContentType())) {
            log.warn("허용되지 않는 파일 형식 - contentType: {}", file.getContentType());
            throw new CommonException(ErrorCode.INVALID_INPUT);
        }
    }
}