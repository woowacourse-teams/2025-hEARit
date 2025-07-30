package com.onair.hearit.admin.application;

import com.onair.hearit.admin.exception.S3Exception;
import java.io.IOException;
import java.io.InputStream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@Slf4j
@Service
@RequiredArgsConstructor
public class S3Uploader {

    private final S3Client s3Client;

    @Value("${aws.s3.bucket}")
    private String bucket;

    public String uploadOriginalAudio(MultipartFile multipartFile) {
        String key = "hearit/audio/original/" + multipartFile.getOriginalFilename();
        return uploadToS3(multipartFile, key);
    }

    public String uploadShortAudio(MultipartFile multipartFile) {
        String key = "hearit/audio/short/" + multipartFile.getOriginalFilename();
        return uploadToS3(multipartFile, key);
    }

    public String uploadScriptFile(MultipartFile multipartFile) {
        String key = "hearit/script/" + multipartFile.getOriginalFilename();
        return uploadToS3(multipartFile, key);
    }

    private String uploadToS3(MultipartFile multipartFile, String key) {
        try (InputStream inputStream = multipartFile.getInputStream()) {
            s3Client.putObject(
                    PutObjectRequest.builder()
                            .bucket(bucket)
                            .key(key)
                            .contentType(multipartFile.getContentType())
                            .build(),
                    RequestBody.fromInputStream(inputStream, multipartFile.getSize())
            );
            return key;
        } catch (IOException e) {
            log.error("S3 파일 업로드 실패: " + multipartFile.getOriginalFilename(), e);
            throw new S3Exception("S3 파일 업로드 실패", e);
        }
    }
}
