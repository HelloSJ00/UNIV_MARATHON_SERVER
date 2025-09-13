package com.runningRank.runningRank.certificateProcessingJob.service;

import com.runningRank.runningRank.certificateProcessingJob.dto.RetryGptJob;
import com.runningRank.runningRank.certificateProcessingJob.dto.RetryOcrJob;
import com.runningRank.runningRank.certificateProcessingJob.respository.CertificateProcessingJobJdbcRepository;
import com.runningRank.runningRank.certificateProcessingJob.respository.CertificateProcessingJobRepository;
import com.runningRank.runningRank.recordVerification.dto.GptSendRequest;
import com.runningRank.runningRank.recordVerification.dto.OcrSendRequest;
import com.runningRank.runningRank.recordVerification.service.RecordVerificationQueueSendService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CertificateProcessingJobRetryService {
    private final CertificateProcessingJobJdbcRepository jobRepository;
    private final RecordVerificationQueueSendService recordVerificationQueueService;

    /**
     * Ocr 재시도 로직
     * 1. Job들 중 생성 시간이 10분이 넘었는데 ALL_DONE이 아닌 잡들은 실패로 간주
     * 2. 실패한 JOB들중 PENDING || OCR_QUEUE_SEND || OCR_QUEUE_FAILED인 JOB들 조회해서 OCR QUEUE SEND 재시도
     */
    public void retryOcr() {
        List<RetryOcrJob> retryOcrJobs = jobRepository.findRetryOcrJob();
        for(RetryOcrJob retryOcrJob : retryOcrJobs) {
            retrySendOcrJob(retryOcrJob);
        }
    }

    /**
     * GPT 재시도 로직
     * 1. Job들 중 생성 시간이 10분이 넘었는데 ALL_DONE이 아닌 잡들은 실패로 간주
     * 2. 실패한 JOB들중 OCR_DONE || GPT_QUEUE_SEND || GPT_QUEUE_FAILED 인 JOB들 조회해서 GPT QUEUE SEND 재시도
     */
    public void retryGpt() {
        List<RetryGptJob> retryGptJobs = jobRepository.findRetryGptJob();
        for(RetryGptJob retryGptJob : retryGptJobs) {
            retrySendGptJob(retryGptJob);
        }
    }

    private void retrySendOcrJob(RetryOcrJob retryOcrJob) {
        OcrSendRequest ocrSendRequest = OcrSendRequest.ofRetry(retryOcrJob);
        recordVerificationQueueService.sendOcrJob(ocrSendRequest);
    }

    private void retrySendGptJob(RetryGptJob retryGptJob) {
        GptSendRequest gptSendRequest = GptSendRequest.ofRetry(retryGptJob);
        recordVerificationQueueService.sendGptJob(gptSendRequest);
    }
}
