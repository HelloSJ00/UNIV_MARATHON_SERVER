package com.runningRank.runningRank.recordVerification.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class OcrCallbackRequest {
    private Long userId;
    private String jobId;              // UUID string
    private String s3TextUrl;     // S3 key or URL
    private String s3ImageUrl;

    public UUID getJobIdAsUuid() {
        return UUID.fromString(jobId);
    }
}