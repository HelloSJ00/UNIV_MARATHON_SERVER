package com.runningRank.runningRank.certificateProcessingJob.domain;

public enum JobStatus {
    PENDING,
    OCR_QUEUE_SEND_DONE,
    OCR_QUEUE_SEND_FAILED,
    OCR_DONE,
    GPT_QUEUE_SEND_DONE,
    GPT_QUEUE_SEND_FAILED,
    GPT_DONE,
    FAILED,
    ALL_DONE
}
