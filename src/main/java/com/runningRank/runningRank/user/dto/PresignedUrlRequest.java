package com.runningRank.runningRank.user.dto;

import lombok.Getter;

@Getter
public class PresignedUrlRequest {
    private String fileName;
    private String fileType;
}

