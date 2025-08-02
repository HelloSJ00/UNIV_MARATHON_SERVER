package com.runningRank.runningRank.recordVerification.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.runningRank.runningRank.certificateProcessingJob.domain.CertificateProcessingJob;
import com.runningRank.runningRank.certificateProcessingJob.domain.JobStatus;
import com.runningRank.runningRank.certificateProcessingJob.respository.CertificateProcessingJobRepository;
import com.runningRank.runningRank.emailVerification.domain.VerificationStatus;
import com.runningRank.runningRank.recordUploadLog.service.RecordUploadLogService;
import com.runningRank.runningRank.recordVerification.domain.RecordVerification;
import com.runningRank.runningRank.recordVerification.dto.RecordInfo;
import com.runningRank.runningRank.recordVerification.exception.CallQuotaExceededException;
import com.runningRank.runningRank.recordVerification.repository.RecordVerificationRepository;
import com.runningRank.runningRank.runningRecord.domain.RunningType;
import com.runningRank.runningRank.user.domain.User;
import com.runningRank.runningRank.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;

import java.io.InputStream;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class RecordVerificationService {

    private final RecordVerificationRepository recordVerificationRepository;
    private final CertificateProcessingJobRepository certificateProcessingJobRepository;
    private final RecordVerificationQueueSendService recordVerificationQueueSendService;
    private final RecordUploadLogService recordUploadLogService;
    private final UserRepository userRepository;
    private final S3Client s3Client;
    private static final String RESULT_BUCKET = "univ-marathon-rank";

    // 기록 인증 검증 객체 생성 로직
    public UUID createRecordVerification(Long userId, String s3ImageUrl) {
        if (!recordUploadLogService.checkUserCanCall(userId)) {
            throw new CallQuotaExceededException();
        }

        log.info("🚀 기록 검증 Job 생성 시작: {}", s3ImageUrl);

        UUID jobId = UUID.randomUUID();

        // 1. DB에 PENDING 상태로 Job 저장
        CertificateProcessingJob job = CertificateProcessingJob.builder()
                .id(jobId)
                .user(userRepository.getReferenceById(userId))
                .originalS3Url(s3ImageUrl)
                .status(JobStatus.PENDING)
                .build();

        certificateProcessingJobRepository.save(job);

        // 2. OCR SQS 큐에 메시지 전송
        try{
            recordVerificationQueueSendService.sendOcrJob(userId, jobId, s3ImageUrl);
        }
        catch (Exception e) {
            log.error("🚨 OCR SQS 큐에 메시지 전송 실패. userId: {}, jobId: {}", userId, jobId, e);
            throw new RuntimeException("OCR SQS 큐에 메시지 전송 실패: " + e.getMessage(), e);
        }

        return jobId;
    }

    public String downloadAndParseFormattedResult(String s3Key) {
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
    public InputStream downloadS3File(String bucket, String key) {
        GetObjectRequest request = GetObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .build();
        return s3Client.getObject(request);
    }

    // 콜백으로 들어온 기록 인증 요청 데이터 저장
    public void saveRecordVerification(Long userId, String s3ImageUrl, String formattedText) {
        try {
            RecordInfo info = RecordVerificationUtil.parseRecordInfo(formattedText);
            log.debug("Parsed RecordInfo - marathonName: {}, runningType: {}, record: {}",
                    info.getMarathonName(), info.getRunningType(), info.getRecord());

            User user = userRepository.getReferenceById(userId);
            RunningType runningType = RunningType.valueOf(info.getRunningType());

            deletePendingIfExists(userId, runningType);

            RecordVerification recordVerification = RecordVerification.of(user, s3ImageUrl, info);
            recordVerificationRepository.save(recordVerification);

            log.info("새로운 RecordVerification 저장 완료: userId={}, runningType={}, marathonName={}",
                    userId, runningType, info.getMarathonName());

        } catch (Exception e) {
            log.error("기록 인증 요청 처리 실패: JSON 파싱 또는 저장 중 예외 발생", e);
            throw new RuntimeException("기록 파싱 또는 저장 실패", e);
        }

        recordUploadLogService.increaseCallCount(userId);
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
}
