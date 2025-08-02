package com.runningRank.runningRank.recordVerification.service;

import com.runningRank.runningRank.certificateProcessingJob.domain.CertificateProcessingJob;
import com.runningRank.runningRank.certificateProcessingJob.domain.JobStatus;
import com.runningRank.runningRank.certificateProcessingJob.respository.CertificateProcessingJobRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class RecordVerificationCallbackService {

    private final CertificateProcessingJobRepository certificateProcessingJobRepository;
    private final RecordVerificationService recordVerificationService;
    private final RecordVerificationQueueSendService recordVerificationQueueSendService;

    public void handleOcrCallback(Long userId, UUID jobId, String s3ImageUrl, String s3TextUrl) {
        CertificateProcessingJob job = certificateProcessingJobRepository.findById(jobId)
                .orElseThrow(() -> new IllegalArgumentException("📛 Job이 존재하지 않습니다. jobId = " + jobId));

        // 이미 OCR_DONE이거나 FAILED면 무시
        if (job.getStatus() == JobStatus.OCR_DONE || job.getStatus() == JobStatus.FAILED) {
            throw new IllegalStateException("⛔ 이미 처리 완료된 Job입니다. 현재 상태: " + job.getStatus());
        }

        // 1. OCR 결과 저장
        job.setOcrResultUrl(s3TextUrl);

        // 2. 상태 업데이트
        job.setStatus(JobStatus.OCR_DONE);

        certificateProcessingJobRepository.save(job);

        // 3. GPT Lambda 비동기 호출 (SQS 메시지 전송)
        recordVerificationQueueSendService.sendGptJob(userId,job.getId(), s3ImageUrl, s3TextUrl);

        log.info("✅ OCR 처리 완료 및 GPT 작업 요청 완료: jobId={}, ocrKey={}", jobId, s3TextUrl);
    }

    public void handleGptCallback(Long userId, UUID jobId,String s3ImageUrl, String gptResultS3Key) {
        CertificateProcessingJob job = certificateProcessingJobRepository.findById(jobId)
                .orElseThrow(() -> new IllegalArgumentException("📛 Job이 존재하지 않습니다. jobId = " + jobId));

        // 이미 GPT_DONE이거나 FAILED면 무시
        if (job.getStatus() == JobStatus.GPT_DONE || job.getStatus() == JobStatus.FAILED) {
            throw new IllegalStateException("⛔ 이미 처리 완료된 Job입니다. 현재 상태: " + job.getStatus());
        }

        // 1. GPT 결과 저장
        job.setGptResultUrl(gptResultS3Key);
//        String jsonFromGpt = extractFormattedResultS3Key(gptResultS3Key);
        String formattedText = recordVerificationService.downloadAndParseFormattedResult(gptResultS3Key);

        recordVerificationService.saveRecordVerification(userId, s3ImageUrl, formattedText);

        // 2. 상태 업데이트
        job.setStatus(JobStatus.GPT_DONE);

        certificateProcessingJobRepository.save(job);

        log.info("✅ GPT 처리 완료: jobId={}, formattedKey={}", jobId, formattedText);
    }
}
