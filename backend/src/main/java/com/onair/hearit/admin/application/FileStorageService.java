package com.onair.hearit.admin.application;

import com.onair.hearit.admin.exception.AdminFileException;
import com.onair.hearit.domain.FileType;
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
import software.amazon.awssdk.services.s3.model.S3Exception;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileStorageService {

    private final S3Client s3Client;

    @Value("${aws.s3.bucket}")
    private String bucket;

    public String uploadFile(MultipartFile multipartFile, FileType fileType) {
        String key = fileType.getUploadPath() + multipartFile.getOriginalFilename();
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
            return "/" + key;
        } catch (IOException e) {
            throw new AdminFileException("업로드 된 파일을 읽어오는데 실패했습니다.", e);
        } catch (S3Exception e) {
            throw new AdminFileException("S3 파일 업로드 실패했습니다.", e);
        }
    }

    public void deleteFile(String filePath) {
        if(filePath.startsWith("/")) {
            filePath = filePath.substring(1);
        }
        String key = filePath;
        try {
            s3Client.deleteObject(builder -> builder.bucket(bucket).key(key).build());
        } catch (S3Exception e) {
            throw new AdminFileException("S3 삭제 실패했습니다.", e);
        }
    }
}
