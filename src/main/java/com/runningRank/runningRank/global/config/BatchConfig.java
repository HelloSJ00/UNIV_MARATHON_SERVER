package com.runningRank.runningRank.global.config;

import com.runningRank.runningRank.certificateProcessingJob.domain.CertificateProcessingJob;
import com.runningRank.runningRank.certificateProcessingJob.service.CertificateProcessingJobRetryService;
import org.springframework.batch.core.Job;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@EnableBatchProcessing
@RequiredArgsConstructor
public class BatchConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;

    @Bean
    public Job ocrRetryJob(Step ocrRetryStep) {
        return new JobBuilder("ocrRetryJob", jobRepository)
                .start(ocrRetryStep)
                .build();
    }

    @Bean
    public Step ocrRetryStep(CertificateProcessingJobRetryService retryService) {
        return new StepBuilder("ocrRetryStep", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    retryService.retryOcr();
                    return RepeatStatus.FINISHED;
                }, transactionManager)
                .build();
    }

    @Bean
    public Job gptRetryJob(Step gptRetryStep) {
        return new JobBuilder("gptRetryJob", jobRepository)
                .start(gptRetryStep)
                .build();
    }

    @Bean
    public Step gptRetryStep(CertificateProcessingJobRetryService retryService) {
        return new StepBuilder("gptRetryStep", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    retryService.retryGpt();
                    return RepeatStatus.FINISHED;
                }, transactionManager)
                .build();
    }
}
