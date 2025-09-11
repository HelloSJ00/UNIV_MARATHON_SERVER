package com.runningRank.runningRank.recordVerification.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Builder
@Getter
@Setter
public class GptSendRequest {
    private Long userId;
    private String jobId;              // UUID string
    private String s3TextUrl;     // S3 key or URL
    private String s3ImageUrl;

    public UUID getJobIdAsUuid() {
        return UUID.fromString(jobId);
    }

    public static GptSendRequest of(OcrCallbackRequest req){
        return GptSendRequest.builder()
                .userId(req.getUserId())
                .jobId(req.getJobId())
                .s3TextUrl(req.getS3TextUrl())
                .s3ImageUrl(req.getS3ImageUrl())
                .build();
    }
}
