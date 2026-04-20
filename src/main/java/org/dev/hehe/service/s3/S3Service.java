package org.dev.hehe.service.s3;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dev.hehe.common.exception.CommonException;
import org.dev.hehe.common.exception.ErrorCode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.util.UUID;

/**
 * AWS S3 파일 업로드/삭제 서비스
 * - 파일을 S3에 업로드하고 접근 가능한 URL을 반환
 * - 업로드된 파일 삭제 지원
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class S3Service {

    private final S3Client s3Client;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    @Value("${cloud.aws.region}")
    private String region;

    /**
     * S3에 파일 업로드
     *
     * @param file      업로드할 파일
     * @param directory S3 저장 디렉터리 (예: "articles")
     * @return 업로드된 파일의 S3 URL
     * @throws BusinessException 파일 업로드 실패 시 INTERNAL_SERVER_ERROR
     */
    public String upload(MultipartFile file, String directory) {
        String fileName = directory + "/" + UUID.randomUUID() + "_" + file.getOriginalFilename();

        try {
            PutObjectRequest request = PutObjectRequest.builder()
                    .bucket(bucket)
                    .key(fileName)
                    .contentType(file.getContentType())
                    .contentLength(file.getSize())
                    .build();

            s3Client.putObject(request, RequestBody.fromBytes(file.getBytes()));

            String fileUrl = "https://" + bucket + ".s3." + region + ".amazonaws.com/" + fileName;
            log.info("S3 파일 업로드 완료 - url: {}", fileUrl);
            return fileUrl;

        } catch (IOException e) {
            log.warn("S3 파일 업로드 실패 - fileName: {}, error: {}", fileName, e.getMessage());
            throw new CommonException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * S3에서 파일 삭제
     *
     * @param fileUrl 삭제할 파일의 S3 URL
     */
    public void delete(String fileUrl) {
        String key = extractKeyFromUrl(fileUrl);

        DeleteObjectRequest request = DeleteObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .build();

        s3Client.deleteObject(request);
        log.info("S3 파일 삭제 완료 - key: {}", key);
    }

    /**
     * S3 URL에서 파일 키 추출
     *
     * @param fileUrl S3 파일 URL
     * @return S3 파일 키 (예: articles/uuid_image.jpg)
     */
    private String extractKeyFromUrl(String fileUrl) {
        return fileUrl.substring(fileUrl.indexOf(".amazonaws.com/") + ".amazonaws.com/".length());
    }
}