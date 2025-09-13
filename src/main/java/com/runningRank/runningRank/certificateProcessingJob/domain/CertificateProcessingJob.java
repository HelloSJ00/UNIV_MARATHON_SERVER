package com.runningRank.runningRank.certificateProcessingJob.domain;

import com.runningRank.runningRank.user.domain.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "certificate_processing_job")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CertificateProcessingJob {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "original_s3_url", nullable = false, length = 1000)
    private String originalS3Url;

    @Column(name = "ocr_result_url", length = 1000)
    private String ocrResultUrl;

    @Column(name = "gpt_result_url", length = 1000)
    private String gptResultUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private JobStatus status;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "retry_count", nullable = false)
    private int retryCount = 0;

    public void jobStatusUpdateFromOcr(String s3TextUrl){
        // 이미 OCR_DONE이거나 FAILED면 무시
        if (this.getStatus() == JobStatus.OCR_DONE || this.getStatus() == JobStatus.FAILED) {
            throw new IllegalStateException("⛔ 이미 처리 완료된 Job입니다. 현재 상태: " + this.getStatus());
        }

        this.status = JobStatus.OCR_DONE;
        this.ocrResultUrl = s3TextUrl;
    }

    public void jobStatusUpdateFromGpt(String gptResultS3Key){
        // 이미 OCR_DONE이거나 FAILED면 무시
        if (this.getStatus() == JobStatus.GPT_DONE || this.getStatus() == JobStatus.FAILED) {
            throw new IllegalStateException("⛔ 이미 처리 완료된 Job입니다. 현재 상태: " + this.getStatus());
        }

        this.status = JobStatus.GPT_DONE;
        this.gptResultUrl = gptResultS3Key;
    }

    public void ocrQueueSendDone(){
        this.status = JobStatus.OCR_QUEUE_SEND_DONE;
    }

    public void ocrQueueSendFailed(){
        this.status = JobStatus.OCR_QUEUE_SEND_FAILED;
    }

    public void gptQueueSendDone(){
        this.status = JobStatus.GPT_QUEUE_SEND_DONE;
    }

    public void gptQueueSendFailed(){
        this.status = JobStatus.GPT_QUEUE_SEND_FAILED;
    }

    public void failed(String errorMessage){
        this.status = JobStatus.FAILED;
        this.errorMessage = errorMessage;
    }

    public void allDone(){
        this.status = JobStatus.ALL_DONE;
    }

    public void increaseRetryCount() {
        this.retryCount++;
        if (this.retryCount >= 3) {
            this.status = JobStatus.FAILED;
        }
    }

}