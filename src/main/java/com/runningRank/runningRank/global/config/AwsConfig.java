package com.runningRank.runningRank.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.auth.credentials.ProfileCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.lambda.LambdaClient;
import software.amazon.awssdk.services.s3.S3Client;

@Configuration
public class AwsConfig {

    @Configuration
    @Profile("dev")
    static class LocalAwsConfig {

        @Bean
        public S3Client localS3Client() {
            return S3Client.builder()
                    .region(Region.AP_NORTHEAST_2)
                    .credentialsProvider(ProfileCredentialsProvider.create("univ-marathon")) // 로컬에서만 사용
                    .build();
        }

        @Bean
        public LambdaClient localLambdaClient() {
            return LambdaClient.builder()
                    .region(Region.AP_NORTHEAST_2)
                    .credentialsProvider(ProfileCredentialsProvider.create("univ-marathon"))
                    .build();
        }
    }

    @Configuration
    @Profile("prod")
    static class ProdAwsConfig {

        @Bean
        public S3Client prodS3Client() {
            return S3Client.builder()
                    .region(Region.AP_NORTHEAST_2)
                    .build(); // EC2 IAM 역할을 자동 인식
        }

        @Bean
        public LambdaClient prodLambdaClient() {
            return LambdaClient.builder()
                    .region(Region.AP_NORTHEAST_2)
                    .build(); // 마찬가지로 자동
        }
    }
}