package com.runningRank.runningRank.recordVerification.dto;

import com.runningRank.runningRank.certificateProcessingJob.domain.CertificateProcessingJob;
import com.runningRank.runningRank.certificateProcessingJob.dto.RetryOcrJob;
import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Builder
@Getter
public class OcrSendRequest {
    Long userId;
    String jobId;
    String s3ImageUrl;

    public static OcrSendRequest of(CertificateProcessingJob job) {
        return OcrSendRequest.builder()
                .userId(job.getUser().getId())
                .jobId(String.valueOf(job.getId()))
                .s3ImageUrl(job.getOriginalS3Url())
                .build();
    }

    public static OcrSendRequest ofRetry(RetryOcrJob job) {
        return OcrSendRequest.builder()
                .userId(job.getUserId())
                .jobId(job.getJobId())
                .s3ImageUrl(job.getS3ImageUrl())
                .build();
    }

    public UUID getJobIdAsUuid() {
        return UUID.fromString(jobId);
    }
}
