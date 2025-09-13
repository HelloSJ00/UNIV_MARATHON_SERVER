package com.runningRank.runningRank.certificateProcessingJob.dto;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class RetryGptJob {
    private Long userId;
    private String jobId;         // UUID string
    private String s3TextUrl;     // S3 key or URL
    private String s3ImageUrl;
}
