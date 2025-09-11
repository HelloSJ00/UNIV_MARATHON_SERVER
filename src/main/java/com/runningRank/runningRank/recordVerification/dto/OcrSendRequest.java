package com.runningRank.runningRank.recordVerification.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Builder
@Getter
public class OcrSendRequest {
    Long userId;
    String jobId;
    String s3ImageUrl;

    public static OcrSendRequest of(Long userId, UUID jobId, String s3ImageUrl) {
        return OcrSendRequest.builder()
                .userId(userId)
                .jobId(String.valueOf(jobId))
                .s3ImageUrl(s3ImageUrl)
                .build();
    }

    public UUID getJobIdAsUuid() {
        return UUID.fromString(jobId);
    }
}
