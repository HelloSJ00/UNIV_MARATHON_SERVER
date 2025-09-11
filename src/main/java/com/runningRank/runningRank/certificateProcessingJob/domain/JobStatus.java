package com.runningRank.runningRank.certificateProcessingJob.domain;

public enum JobStatus {
    PENDING,
    OCR_QUEUE_SEND_DONE,
    OCR_QUEUE_SEND_FAILED,
    OCR_IN_PROGRESS,
    OCR_DONE,
    GPT_IN_PROGRESS,
    GPT_QUEUE_SEND_DONE,
    GPT_QUEUE_SEND_FAILED,
    GPT_DONE,
    FAILED,
    CANCELLED,
    ALL_DONE
}
