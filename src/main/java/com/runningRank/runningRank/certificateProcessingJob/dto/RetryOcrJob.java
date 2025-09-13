package com.runningRank.runningRank.certificateProcessingJob.dto;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class RetryOcrJob {
    Long userId;
    String jobId;
    String s3ImageUrl;
}
