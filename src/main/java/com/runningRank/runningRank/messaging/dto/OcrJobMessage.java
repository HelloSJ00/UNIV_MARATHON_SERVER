package com.runningRank.runningRank.messaging.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OcrJobMessage {
    private Long userId;
    private UUID jobId;
    private String s3ImageUrl;
}