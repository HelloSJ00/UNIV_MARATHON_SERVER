package com.runningRank.runningRank.recordVerification.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OcrCallbackRequest {
    private Long userId;
    private String jobId;              // UUID string
    private String ocrResultS3Key;     // S3 key or URL
    private String s3ImageUrl;
}