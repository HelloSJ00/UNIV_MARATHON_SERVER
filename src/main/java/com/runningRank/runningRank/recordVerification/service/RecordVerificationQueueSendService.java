package com.runningRank.runningRank.recordVerification.service;

import com.runningRank.runningRank.messaging.GptSqsProducer;
import com.runningRank.runningRank.messaging.OcrSqsProducer;
import com.runningRank.runningRank.recordVerification.dto.GptSendRequest;
import com.runningRank.runningRank.recordVerification.dto.OcrCallbackRequest;
import com.runningRank.runningRank.recordVerification.dto.OcrSendRequest;
import com.runningRank.runningRank.recordVerification.dto.S3ImageSaveRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class RecordVerificationQueueSendService {

    private final OcrSqsProducer ocrSqsProducer;
    private final GptSqsProducer gptSqsProducer;

    public void sendOcrJob(OcrSendRequest request) {
        ocrSqsProducer.sendOcrJob(
                request.getUserId(),
                request.getJobIdAsUuid(),
                request.getS3ImageUrl()); // 예외 발생 시 위로 전달
    }

    public void sendGptJob(GptSendRequest request) {
        gptSqsProducer.sendGptJob(
                request.getUserId(),
                request.getJobIdAsUuid(),
                request.getS3ImageUrl(),
                request.getS3TextUrl()); // 예외 발생 시 위로 전달(jobId, s3ImageUrl, s3TextUrl);
    }
}
