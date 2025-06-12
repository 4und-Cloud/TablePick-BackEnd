package com.goorm.tablepick.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

@Configuration
public class S3Config {

    @Bean
    public S3Client s3Client() {
        String region = "ap-northeast-2";
        return S3Client.builder()
                .region(Region.of(region))
                .build(); // IAM Role을 자동 사용
    }
}

