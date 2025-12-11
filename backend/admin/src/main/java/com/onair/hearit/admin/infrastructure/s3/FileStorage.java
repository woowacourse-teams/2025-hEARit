package com.onair.hearit.admin.infrastructure.s3;

import com.onair.hearit.admin.exception.custom.AdminFileException;
import com.onair.hearit.admin.exception.custom.AdminInvalidInputException;
import com.onair.hearit.core.domain.FileType;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

@RequiredArgsConstructor
public class FileStorage {

    private static final Duration PUT_PRESIGNED_URL_DURATION = Duration.ofMinutes(3);

    private final S3Client s3Client;
    private final S3Presigner s3Presigner;
    private final String bucket;

    public URL createPutUrl(String key) {
        key = validateKey(key);
        PutObjectRequest put = PutObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .build();

        PutObjectPresignRequest presign = PutObjectPresignRequest.builder()
                .putObjectRequest(put)
                .signatureDuration(PUT_PRESIGNED_URL_DURATION)
                .build();

        return s3Presigner.presignPutObject(presign).url();
    }

    public String uploadFile(MultipartFile multipartFile, FileType fileType) {
        String filePath = fileType.getUploadPath() + multipartFile.getOriginalFilename();
        return uploadToS3(multipartFile, filePath);
    }

    private String uploadToS3(MultipartFile multipartFile, String filePath) {
        try (InputStream inputStream = multipartFile.getInputStream()) {
            String key = validateKey(filePath);
            s3Client.putObject(PutObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .contentType(multipartFile.getContentType())
                    .build(), RequestBody.fromInputStream(inputStream, multipartFile.getSize())
            );
            return filePath;
        } catch (IOException e) {
            throw new AdminFileException("업로드 된 파일을 읽어오는데 실패");
        } catch (S3Exception e) {
            throw new AdminFileException("S3 파일 업로드 실패");
        }
    }

    public void deleteFile(String key) {
        try {
            String validateKey = validateKey(key);
            s3Client.deleteObject(builder -> builder
                    .bucket(bucket)
                    .key(validateKey)
                    .build()
            );
        } catch (RuntimeException e) {
            throw new AdminFileException("S3 파일 삭제 실패, key: " + key);
        }
    }

    private String validateKey(String key) {
        if (key == null || key.isBlank()) {
            throw new AdminInvalidInputException("key는 빈 값일 수 없습니다.");
        }
        if (key.startsWith("/")) {
            return key.substring(1);
        }
        return key;
    }
}
