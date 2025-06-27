package com.runningRank.runningRank.recordVerification.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.runningRank.runningRank.emailVerification.domain.VerificationStatus;
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
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RecordVerificationService {

    private final RecordVerificationLambdaClient lambdaClient;
    private final RecordVerificationRepository recordVerificationRepository;
    private final UserRepository userRepository;
    private final S3Client s3Client;
    private final ObjectMapper objectMapper;
    private static final String RESULT_BUCKET = "univ-marathon-rank";

    public void createRecordVerification(Long userId, String s3ImageUrl) {
        try {
            log.info("🚀 기록 검증 시작: {}", s3ImageUrl);

            String ocrResponseJson = callOcrLambda(s3ImageUrl);
            String ocrResultS3Key = extractOcrResultS3Key(ocrResponseJson);

            String gptResponseJson = callGptLambda(ocrResultS3Key);
            String formattedResultS3Key = extractFormattedResultS3Key(gptResponseJson);

            String formattedText = downloadAndParseFormattedResult(formattedResultS3Key);

            saveRecordVerification(userId, s3ImageUrl, formattedText);

        } catch (Exception e) {
            log.error("🚨 기록 검증 중 오류 발생", e);
            throw new RuntimeException("기록 검증 실패", e);
        }
    }

    private String callOcrLambda(String s3ImageUrl) {
        // OCR Lambda 호출
        String ocrResponseJson = lambdaClient.callGoogleVisionOCR(s3ImageUrl);
        log.info("✅ OCR Lambda 응답 JSON: {}", ocrResponseJson);
        return ocrResponseJson;
    }

    private String extractOcrResultS3Key(String ocrResponseJson) {
        try {
            // OCR 결과에서 S3 Key 추출
            JsonNode ocrJson = objectMapper.readTree(ocrResponseJson); // 기존 인스턴스 사용
            String ocrResultS3Key = ocrJson.get("ocrResultS3Key").asText();
            log.info("📁 OCR 결과 S3 Key: {}", ocrResultS3Key);
            return ocrResultS3Key;
        } catch (Exception e) {
            log.error("OCR 응답 JSON 파싱 중 오류 발생: {}", ocrResponseJson, e);
            throw new RuntimeException("OCR 결과 S3 Key 추출 실패", e);
        }
    }

    private String callGptLambda(String ocrResultS3Key) {
        // GPT Lambda 호출
        String gptResponseJson = lambdaClient.callGptFormattingLambda(ocrResultS3Key);
        log.info("✅ GPT Lambda 응답 JSON: {}", gptResponseJson);
        return gptResponseJson;
    }

    public String downloadAndParseFormattedResult(String formattedResultS3Key) {
        // S3에서 해당 JSON 파일 다운로드 요청 객체 생성
        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(RESULT_BUCKET)
                .key(formattedResultS3Key)
                .build();

        log.info("⬇️ S3에서 포맷 결과 JSON 다운로드 시작. Key: {}", formattedResultS3Key);

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
                        formattedResultS3Key, parsedJson.toString());
                log.error(errorMessage);
                throw new IllegalArgumentException(errorMessage);
            }

            String formattedText = formattedTextNode.asText();

            log.info("✅ 최종 포맷된 텍스트: {}", formattedText);
            return formattedText;

        } catch (Exception e) { // 모든 종류의 예외를 여기서 한 번에 처리합니다.
            log.error("🚨 S3 JSON 파일 다운로드 및 파싱 중 알 수 없는 오류 발생. Key: {}", formattedResultS3Key, e);
            throw new RuntimeException("기록 포맷 결과 다운로드 및 파싱 실패: " + e.getMessage(), e);
        }
    }

    private String extractFormattedResultS3Key(String gptResponseJson) {
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
