package com.runningRank.runningRank.recordVerification.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.runningRank.runningRank.certificateProcessingJob.domain.CertificateProcessingJob;
import com.runningRank.runningRank.certificateProcessingJob.domain.JobStatus;
import com.runningRank.runningRank.certificateProcessingJob.respository.CertificateProcessingJobRepository;
import com.runningRank.runningRank.emailVerification.domain.VerificationStatus;
import com.runningRank.runningRank.messaging.GptSqsProducer;
import com.runningRank.runningRank.messaging.OcrSqsProducer;
import com.runningRank.runningRank.recordVerification.domain.RecordVerification;
import com.runningRank.runningRank.recordVerification.dto.RecordInfo;
import com.runningRank.runningRank.recordVerification.repository.RecordVerificationRepository;
import com.runningRank.runningRank.runningRecord.domain.RunningType;
import com.runningRank.runningRank.user.domain.User;
import com.runningRank.runningRank.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class RecordVerificationService {

    private final RecordVerificationRepository recordVerificationRepository;
    private final CertificateProcessingJobRepository certificateProcessingJobRepository;
    private final OcrSqsProducer ocrSqsProducer;
    private final GptSqsProducer gptSqsProducer;
    private final UserRepository userRepository;
    private final S3Client s3Client;
    private final ObjectMapper objectMapper;
    private static final String RESULT_BUCKET = "univ-marathon-rank";

    public UUID createRecordVerification(Long userId, String s3ImageUrl) {
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
        ocrSqsProducer.sendOcrJob(userId, jobId, s3ImageUrl);

        return jobId;
    }

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
        gptSqsProducer.sendGptJob(userId,job.getId(), s3ImageUrl, s3TextUrl);

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
        String formattedText = downloadAndParseFormattedResult(gptResultS3Key);

        saveRecordVerification(userId, s3ImageUrl, formattedText);

        // 2. 상태 업데이트
        job.setStatus(JobStatus.GPT_DONE);

        certificateProcessingJobRepository.save(job);

        log.info("✅ GPT 처리 완료: jobId={}, formattedKey={}", jobId, formattedText);
    }

    public String downloadAndParseFormattedResult(String gptResultS3Key) {
        // S3에서 해당 JSON 파일 다운로드 요청 객체 생성
        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(RESULT_BUCKET)
                .key(gptResultS3Key)
                .build();

        log.info("⬇️ S3에서 포맷 결과 JSON 다운로드 시작. Key: {}", gptResultS3Key);

        // try-with-resources를 사용하여 S3 응답 스트림을 자동 닫고,
        // 발생 가능한 모든 예외를 하나의 RuntimeException으로 래핑합니다.
        try (ResponseInputStream<GetObjectResponse> s3ResponseInputStream = s3Client.getObject(getObjectRequest)) {
            // S3 InputStream을 직접 ObjectMapper로 파싱하여 메모리 효율성 높임
            JsonNode parsedJson = objectMapper.readTree(s3ResponseInputStream);

            // JsonNode를 통째로 로그로 출력하는 것은 큰 JSON 파일의 경우 성능 문제나 로그 폭주를 일으킬 수 있습니다.
            // 디버깅 목적이 아니라면 생략하거나, 필요한 부분만 추출하여 로깅하는 것을 권장합니다.
            log.debug("📦 다운로드 및 파싱된 JSON 내용 (디버그): {}", parsedJson.toString());

            // "formattedText" 필드 안전하게 추출
            JsonNode formattedTextNode = parsedJson.get("formattedText");

            // 필드가 없거나 null인 경우 오류 처리
            if (formattedTextNode == null || formattedTextNode.isMissingNode() || formattedTextNode.isNull()) {
                // 이 부분은 NullPointerException이 아닌 IllegalArgumentException을 명시적으로 던집니다.
                // 이는 JSON 구조가 예상과 다를 때 발생하는 논리적 오류이므로, 다른 기술적 예외와 분리하는 것이 좋습니다.
                String errorMessage = String.format("다운로드된 JSON에 'formattedText' 필드가 없거나 null입니다. Key: %s, JSON: %s",
                        gptResultS3Key, parsedJson.toString());
                log.error(errorMessage);
                throw new IllegalArgumentException(errorMessage);
            }

            String formattedText = formattedTextNode.asText();

            log.info("✅ 최종 포맷된 텍스트: {}", formattedText);
            return formattedText;

        } catch (Exception e) { // 모든 종류의 예외를 여기서 한 번에 처리합니다.
            log.error("🚨 S3 JSON 파일 다운로드 및 파싱 중 알 수 없는 오류 발생. Key: {}", gptResultS3Key, e);
            throw new RuntimeException("기록 포맷 결과 다운로드 및 파싱 실패: " + e.getMessage(), e);
        }
    }

    private String extractFormattedResultS3Key(String gptResponseJson) {
        log.info("🔍 GPT 응답에서 포맷 결과 파일의 S3 Key 추출 시작");
        log.info("📦 GPT 응답 JSON: {}", gptResponseJson);
        try {
            // GPT 응답에서 결과 파일의 S3 Key 추출
            JsonNode gptJson = objectMapper.readTree(gptResponseJson);
            if (gptJson == null || gptJson.get("body") == null) {
                log.warn("GPT 응답에서 'body' 필드를 찾을 수 없습니다.");
                return null;
            }

            JsonNode bodyJson = objectMapper.readTree(gptJson.get("body").asText());
            if (bodyJson == null || bodyJson.get("formattedResultS3Key") == null) {
                log.warn("'body' 내부에서 'formattedResultS3Key' 필드를 찾을 수 없습니다.");
                return null;
            }

            String formattedResultS3Key = bodyJson.get("formattedResultS3Key").asText();
            log.info("📁 포맷된 결과 S3 Key: {}", formattedResultS3Key);
            return formattedResultS3Key;

        } catch (JsonProcessingException e) {
            log.error("GPT 응답 JSON 파싱 중 오류 발생: {}", e.getMessage(), e);
            return null;
        } catch (Exception e) {
            log.error("formattedResultS3Key 추출 중 예상치 못한 오류 발생: {}", e.getMessage(), e);
            return null;
        }
    }
    public void saveRecordVerification(Long userId, String s3ImageUrl, String formattedText) {
        try {
            RecordInfo info = objectMapper.readValue(formattedText, RecordInfo.class);

            log.debug("Parsed RecordInfo - marathonName: {}, runningType: {}, record: {}",
                    info.getMarathonName(), info.getRunningType(), info.getRecord());

            User user = userRepository.getReferenceById(userId);
            RunningType runningType = RunningType.valueOf(info.getRunningType());

            // 1. 기존 동일한 유저 + 러닝타입 + PENDING 상태의 인증 요청 삭제
            Optional<RecordVerification> existing = recordVerificationRepository
                    .findByUserIdAndRunningTypeAndStatus(userId, runningType, VerificationStatus.PENDING);

            if (existing.isPresent()) {
                log.info("기존 RecordVerification(PENDING) 존재함 → 삭제 시도: userId={}, runningType={}", userId, runningType);
                recordVerificationRepository.delete(existing.get());
                recordVerificationRepository.flush(); // 유니크 제약 위반 방지
                log.info("기존 RecordVerification 삭제 완료");
            }

            // 2. 새로운 인증 요청 생성 및 저장
            RecordVerification recordVerification = RecordVerification.builder()
                    .user(user)
                    .imageUrl(s3ImageUrl)
                    .marathonName(info.getMarathonName())
                    .runningType(runningType)
                    .recordTime(convertToSeconds(info.getRecord()))
                    .status(VerificationStatus.PENDING)
                    .build();

            recordVerificationRepository.save(recordVerification);
            log.info("새로운 RecordVerification 저장 완료: userId={}, runningType={}, marathonName={}",
                    userId, runningType, info.getMarathonName());

        } catch (Exception e) {
            log.error("기록 인증 요청 처리 실패: JSON 파싱 또는 저장 중 예외 발생", e);
            throw new RuntimeException("기록 파싱 또는 저장 실패", e);
        }
    }

    private int convertToSeconds(String hhmmss) {
        String[] parts = hhmmss.split(":");
        int hour = Integer.parseInt(parts[0]);
        int minute = Integer.parseInt(parts[1]);
        int second = Integer.parseInt(parts[2]);
        return hour * 3600 + minute * 60 + second;
    }
}
