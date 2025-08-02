package com.runningRank.runningRank.recordVerification.service;

import com.runningRank.runningRank.messaging.GptSqsProducer;
import com.runningRank.runningRank.messaging.OcrSqsProducer;
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

    public boolean sendOcrJob(Long userId, UUID jobId, String s3ImageUrl) {
        try{
            ocrSqsProducer.sendOcrJob(userId, jobId, s3ImageUrl);
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    public boolean sendGptJob(Long userId, UUID jobId, String s3ImageUrl,String s3TextUrl) {
        try{
            gptSqsProducer.sendGptJob(userId, jobId, s3ImageUrl, s3TextUrl);
        } catch (Exception e) {
            return false;
        }
        return true;
    }
}
