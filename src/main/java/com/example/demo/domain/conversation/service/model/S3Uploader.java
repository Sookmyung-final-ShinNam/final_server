package com.example.demo.domain.conversation.service.model;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.*;
import com.example.demo.apiPayload.code.exception.CustomException;
import com.example.demo.apiPayload.status.ErrorStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Files;

@Slf4j
@Component
@RequiredArgsConstructor
public class S3Uploader {

    private final AmazonS3 amazonS3;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    /**
     * S3에 파일 업로드 후 브라우저에서 바로 열리도록 설정
     */
    public String uploadFileFromFile(File file, String folderName, String fileName) {
        try (InputStream input = new FileInputStream(file)) {

            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentLength(file.length());

            // 확장자 기반 또는 파일 내용 기반 Content-Type 지정
            String contentType = detectContentType(fileName, file);
            metadata.setContentType(contentType);

            // 브라우저에서 바로 보기
            metadata.setContentDisposition("inline; filename=\"" + fileName + "\"");

            String key = folderName + "/" + fileName;
            amazonS3.putObject(new PutObjectRequest(bucket, key, input, metadata));

            log.info("[S3] 파일 업로드 완료, key={}, contentType={}", key, contentType);
            return amazonS3.getUrl(bucket, key).toString();

        } catch (Exception e) {
            log.error("[S3] 파일 업로드 실패", e);
            throw new CustomException(ErrorStatus.FILE_UPLOAD_FAILED);
        }
    }

    /**
     * 파일 확장자 기반 + 실제 파일 내용 기반 Content-Type 감지
     */
    private String detectContentType(String fileName, File file) {

        // 확장자 기반 우선
        if (fileName.endsWith(".mp4")) return "video/mp4";
        if (fileName.endsWith(".png")) return "image/png";
        if (fileName.endsWith(".jpg") || fileName.endsWith(".jpeg")) return "image/jpeg";
        if (fileName.endsWith(".gif")) return "image/gif";

        // 파일 내용 기반 probe
        try {
            String contentType = Files.probeContentType(file.toPath());
            if (contentType != null) return contentType;
        } catch (Exception ignored) { }

        return "application/octet-stream";

    }

}