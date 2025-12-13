package com.onair.hearit.admin.infrastructure.s3;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

@Configuration
public class S3Config {

    private final String bucket;

    public S3Config(@Value("${aws.s3.bucket}") String bucket) {
        this.bucket = bucket;
    }

    @Bean
    public S3Client s3Client() {
        return S3Client.builder()
                .region(Region.AP_NORTHEAST_2)
                .build();
    }

    @Bean
    public S3Presigner s3Presigner() {
        return S3Presigner.builder()
                .region(Region.AP_NORTHEAST_2)
                .build();
    }

    @Bean
    public FileStorage fileStorage(S3Client s3Client, S3Presigner s3Presigner) {
        return new FileStorage(s3Client, s3Presigner, bucket);
    }
}
