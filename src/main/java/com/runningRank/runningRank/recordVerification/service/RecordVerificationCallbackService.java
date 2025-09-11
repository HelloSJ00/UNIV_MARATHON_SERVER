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
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;

import java.io.InputStream;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class RecordVerificationCallbackService {

    private final CertificateProcessingJobRepository certificateProcessingJobRepository;
    private final RecordVerificationService recordVerificationService;
    private final RecordVerificationQueueSendService recordVerificationQueueSendService;
    private final S3Client s3Client;
    private static final String RESULT_BUCKET = "univ-marathon-rank";

    public void handleOcrCallback(OcrCallbackRequest req) {
        CertificateProcessingJob job = certificateProcessingJobRepository.findById(req.getJobIdAsUuid())
                .orElseThrow(() -> new IllegalArgumentException("📛 Job이 존재하지 않습니다. jobId = " + req.getJobIdAsUuid()));
        job.jobStatusUpdateFromOcr(req.getS3TextUrl());
        sendGptOrFail(req,job);
    }

    public void handleGptCallback(GptCallbackRequest req) {
        CertificateProcessingJob job = certificateProcessingJobRepository.findById(req.getJobIdAsUuid())
                .orElseThrow(() -> new IllegalArgumentException("📛 Job이 존재하지 않습니다. jobId = " + req.getJobIdAsUuid()));
        job.jobStatusUpdateFromGpt(req.getGptResultS3Url());
        recordVerificationService.saveRecordVerification(req, downloadAndParseFormattedResult(req.getGptResultS3Url()),job);
    }

    private void sendGptOrFail(OcrCallbackRequest req,CertificateProcessingJob job){
        try{
            recordVerificationQueueSendService.sendGptJob(GptSendRequest.of(req));
            job.gptQueueSendDone();
            log.info("✅ OCR 처리 완료 및 GPT 작업 요청 완료: jobId={}, ocrKey={}", req.getJobIdAsUuid(), req.getS3TextUrl());

        } catch (Exception e) {
            job.gptQueueSendFailed();
            log.error("🚨 GPT SQS 큐 전송 실패. userId={}, jobId={}", req.getUserId(), req.getJobIdAsUuid(), e);
            throw new RuntimeException("GPT SQS 큐 전송 실패", e);
        }
    }

    private String downloadAndParseFormattedResult(String s3Key) {
        try (InputStream is =downloadS3File(RESULT_BUCKET, s3Key)) {
            JsonNode json =  RecordVerificationUtil.parseJson(is);
            String result =  RecordVerificationUtil.extractFormattedText(json, s3Key);
            log.info("✅ 최종 포맷 텍스트: {}", result);
            return result;
        } catch (Exception e) {
            log.error("🚨 전체 처리 실패. key: {}", s3Key, e);
            throw new RuntimeException("전체 처리 실패: " + e.getMessage(), e);
        }
    }

    // S3에서 저장한 기록 JSON 파일 다운로드
    private InputStream downloadS3File(String bucket, String key) {
        GetObjectRequest request = GetObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .build();
        return s3Client.getObject(request);
    }
}
