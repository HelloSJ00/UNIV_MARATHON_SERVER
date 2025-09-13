package com.runningRank.runningRank.recordVerification.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.runningRank.runningRank.certificateProcessingJob.domain.CertificateProcessingJob;
import com.runningRank.runningRank.certificateProcessingJob.respository.CertificateProcessingJobRepository;
import com.runningRank.runningRank.recordVerification.dto.GptCallbackRequest;
import com.runningRank.runningRank.recordVerification.dto.GptSendRequest;
import com.runningRank.runningRank.recordVerification.dto.OcrCallbackRequest;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class RecordVerificationCallbackService {

    private final CertificateProcessingJobRepository certificateProcessingJobRepository;
    private final RecordVerificationService recordVerificationService;
    private final RecordVerificationQueueSendService recordVerificationQueueSendService;

    public void handleOcrCallback(OcrCallbackRequest req) {
        CertificateProcessingJob job = certificateProcessingJobRepository.findById(req.getJobIdAsUuid())
                .orElseThrow(() -> new IllegalArgumentException("📛 Job이 존재하지 않습니다. jobId = " + req.getJobIdAsUuid()));
        job.jobStatusUpdateFromOcr(req.getS3TextUrl());
        sendGptOrFail(job);
    }

    public void handleGptCallback(GptCallbackRequest req) {
        CertificateProcessingJob job = certificateProcessingJobRepository.findById(req.getJobIdAsUuid())
                .orElseThrow(() -> new IllegalArgumentException("📛 Job이 존재하지 않습니다. jobId = " + req.getJobIdAsUuid()));
        job.jobStatusUpdateFromGpt(req.getGptResultS3Url());
        recordVerificationService.saveRecordVerification(job);
    }

    private void sendGptOrFail(CertificateProcessingJob job){
        GptSendRequest req = GptSendRequest.of(job);
        try{
            recordVerificationQueueSendService.sendGptJob(req);
            job.gptQueueSendDone();
            log.info("✅ OCR 처리 완료 및 GPT 작업 요청 완료: jobId={}, ocrKey={}", req.getJobIdAsUuid(), req.getS3TextUrl());

        } catch (Exception e) {
            job.gptQueueSendFailed();
            log.error("🚨 GPT SQS 큐 전송 실패. userId={}, jobId={}", req.getUserId(), req.getJobIdAsUuid(), e);
            throw new RuntimeException("GPT SQS 큐 전송 실패", e);
        }
    }
}
