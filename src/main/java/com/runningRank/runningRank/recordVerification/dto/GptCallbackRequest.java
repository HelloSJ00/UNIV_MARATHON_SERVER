package com.runningRank.runningRank.recordVerification.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class GptCallbackRequest {
    private Long userId;
    private String jobId;
    private String gptResultS3Url;
    private String s3ImageUrl;

    public UUID getJobIdAsUuid() {
        return UUID.fromString(jobId);
    }
}
