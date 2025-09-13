package com.runningRank.runningRank.certificateProcessingJob.service;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.batch.core.Job;

@Service
@RequiredArgsConstructor
public class CertificateProcessingJobBatchService {
    private final CertificateProcessingJobRetryService certificateProcessingJobRetryService;
    private final JobLauncher jobLauncher;
    private final Job ocrRetryJob;
    private final Job gptRetryJob;

    /**
     * OCR 재시도 Job - 매 정각 실행
     */
    @Scheduled(cron = "0 0 * * * *") // 매 시 정각
    public void runOcrRetryJob() throws Exception {
        jobLauncher.run(ocrRetryJob,
                new JobParametersBuilder()
                        .addLong("time", System.currentTimeMillis()) // 중복 방지
                        .toJobParameters());
    }

    /**
     * GPT 재시도 Job - 매 시 정각에서 5분 후 실행
     */
    @Scheduled(cron = "0 5 * * * *") // 매 시 5분
    public void runGptRetryJob() throws Exception {
        jobLauncher.run(gptRetryJob,
                new JobParametersBuilder()
                        .addLong("time", System.currentTimeMillis())
                        .toJobParameters());
    }
}
