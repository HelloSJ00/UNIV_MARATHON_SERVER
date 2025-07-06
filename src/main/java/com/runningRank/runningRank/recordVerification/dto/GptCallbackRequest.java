package com.runningRank.runningRank.recordVerification.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GptCallbackRequest {
    private Long userId;
    private String jobId;
    private String gptResultS3Key;
    private String s3ImageUrl;
}
