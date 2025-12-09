package com.onair.hearit.admin.infrastructure.s3;

import java.net.URL;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

@RequiredArgsConstructor
public class PresignedUrlService {

    private static final Duration PUT_PRESIGNED_URL_DURATION = Duration.ofMinutes(3);

    private final S3Presigner s3Presigner;
    private final String bucket;

    public URL createPutUrl(String key) {
        if (key.startsWith("/")) {
            key = key.substring(1);
        }

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
}
