package com.runningRank.runningRank.recordVerification.service;

import com.runningRank.runningRank.certificateProcessingJob.domain.CertificateProcessingJob;
import com.runningRank.runningRank.certificateProcessingJob.domain.JobStatus;
import com.runningRank.runningRank.certificateProcessingJob.respository.CertificateProcessingJobRepository;
import com.runningRank.runningRank.emailVerification.domain.VerificationStatus;
import com.runningRank.runningRank.recordUploadLog.service.RecordUploadLogService;
import com.runningRank.runningRank.recordVerification.domain.RecordVerification;
import com.runningRank.runningRank.recordVerification.dto.GptCallbackRequest;
import com.runningRank.runningRank.recordVerification.dto.OcrSendRequest;
import com.runningRank.runningRank.recordVerification.dto.RecordInfo;
import com.runningRank.runningRank.recordVerification.dto.S3ImageSaveRequest;
import com.runningRank.runningRank.recordVerification.exception.CallQuotaExceededException;
import com.runningRank.runningRank.recordVerification.repository.RecordVerificationRepository;
import com.runningRank.runningRank.runningRecord.domain.RunningType;
import com.runningRank.runningRank.user.domain.User;
import com.runningRank.runningRank.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.util.Optional;
import java.util.UUID;

import static com.runningRank.runningRank.recordVerification.service.RecordVerificationUtil.parseRecordInfoOrThrow;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class RecordVerificationService {
    private final RecordVerificationRepository recordVerificationRepository;
    private final CertificateProcessingJobRepository certificateProcessingJobRepository;
    private final RecordVerificationQueueSendService recordVerificationQueueSendService;
    private final RecordUploadLogService recordUploadLogService;
    private final UserRepository userRepository;

    // 기록 인증 검증 객체 생성 로직
    public UUID createRecordVerification(Long userId, S3ImageSaveRequest req) {
        // 달에 3번이상 요청했는지
        if (!recordUploadLogService.checkUserCanCall(userId)) {
            throw new CallQuotaExceededException();
        }
        log.info("🚀 기록 검증 Job 생성 시작: {}", req.getS3ImageUrl());
        // 1. DB에 PENDING 상태로 Job 저장
        CertificateProcessingJob job = CertificateProcessingJob.builder()
                .id(UUID.randomUUID())
                .user(userRepository.getReferenceById(userId))
                .originalS3Url(req.getS3ImageUrl())
                .status(JobStatus.PENDING)
                .build();

        // 2. OCR SQS 큐에 메시지 전송
        sendOcrOrFail(OcrSendRequest.of(userId, job.getId(), req.getS3ImageUrl()), job);

        // 3. 처음으로 영속성에 넣어줘야 하므로 save 호출
        certificateProcessingJobRepository.save(job);
        return job.getId();
    }

    // 콜백으로 들어온 기록 인증 요청 데이터 저장
    public void saveRecordVerification(GptCallbackRequest req, String formattedText,CertificateProcessingJob job) {
        RecordInfo info = parseRecordInfoOrThrow(formattedText);
        RunningType runningType = RunningType.valueOf(info.getRunningType());
        User user = userRepository.getReferenceById(req.getUserId());

        deletePendingIfExists(req.getUserId(), runningType);

        // 기록 검증 객체 저장
        RecordVerification recordVerification = RecordVerification.of(user, req, info);
        recordVerificationRepository.save(recordVerification);
        log.info("✅ 새로운 RecordVerification 저장 완료: userId={}, runningType={}, marathonName={}",
                req.getUserId(), runningType, info.getMarathonName());

        recordUploadLogService.increaseCallCount(req.getUserId());
        job.allDone();
    }

    private void deletePendingIfExists(Long userId, RunningType runningType) {
        Optional<RecordVerification> existing = recordVerificationRepository
                .findByUserIdAndRunningTypeAndStatus(userId, runningType, VerificationStatus.PENDING);

        existing.ifPresent(ev -> {
            log.info("기존 RecordVerification(PENDING) 존재 → 삭제: userId={}, runningType={}", userId, runningType);
            recordVerificationRepository.delete(ev);
            recordVerificationRepository.flush(); // 유니크 제약 방지
        });
    }

    private void sendOcrOrFail(OcrSendRequest req, CertificateProcessingJob job) {
        try {
            recordVerificationQueueSendService.sendOcrJob(req);
            job.ocrQueueSendDone();
            log.info("🚀 OCR SQS 큐 전송 완료. userId={}, jobId={}", req.getUserId(), req.getJobIdAsUuid());
        } catch (Exception e) {
            job.ocrQueueSendFailed();
            log.error("🚨 OCR SQS 큐 전송 실패. userId={}, jobId={}", req.getUserId(), req.getJobIdAsUuid(), e);
            throw new RuntimeException("OCR SQS 큐 전송 실패", e);
        }
    }


}
