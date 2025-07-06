package com.runningRank.runningRank.certificateProcessingJob.domain;

public enum JobStatus {
    PENDING,
    OCR_IN_PROGRESS,
    OCR_DONE,
    GPT_IN_PROGRESS,
    GPT_DONE,
    FAILED,
    CANCELLED
}
