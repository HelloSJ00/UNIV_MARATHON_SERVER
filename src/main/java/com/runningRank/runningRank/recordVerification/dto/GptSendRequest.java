package com.runningRank.runningRank.recordVerification.dto;

import com.runningRank.runningRank.certificateProcessingJob.domain.CertificateProcessingJob;
import com.runningRank.runningRank.certificateProcessingJob.dto.RetryGptJob;
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

    public static GptSendRequest of(CertificateProcessingJob job){
        return GptSendRequest.builder()
                .userId(job.getUser().getId())
                .jobId(String.valueOf(job.getId()))
                .s3TextUrl(job.getOcrResultUrl())
                .s3ImageUrl(job.getOriginalS3Url())
                .build();
    }

    public static GptSendRequest ofRetry(RetryGptJob job) {
        return GptSendRequest.builder()
                .userId(job.getUserId())
                .jobId(job.getJobId())
                .s3TextUrl(job.getS3TextUrl())
                .s3ImageUrl(job.getS3ImageUrl())
                .build();
    }
}
