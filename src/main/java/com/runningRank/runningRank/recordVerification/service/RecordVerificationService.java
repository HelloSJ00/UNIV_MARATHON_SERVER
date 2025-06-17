package com.runningRank.runningRank.recordVerification.service;

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
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
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

    public void createRecordVerification(Long userId,String s3ImageUrl) {
        try {
            log.info("🚀 기록 검증 시작: {}", s3ImageUrl);

            // 1. OCR Lambda 호출
            String ocrResponseJson = lambdaClient.callGoogleVisionOCR(s3ImageUrl);
            log.info("✅ OCR Lambda 응답 JSON: {}", ocrResponseJson);

            // 2. OCR 결과에서 S3 Key 추출
            JsonNode ocrJson = objectMapper.readTree(ocrResponseJson);
            String ocrResultS3Key = ocrJson.get("ocrResultS3Key").asText();
            log.info("📁 OCR 결과 S3 Key: {}", ocrResultS3Key);

            // 3. GPT Lambda 호출
            String gptResponseJson = lambdaClient.callGptFormattingLambda(ocrResultS3Key);
            log.info("✅ GPT Lambda 응답 JSON: {}", gptResponseJson);

            // 4. GPT 응답에서 결과 파일의 S3 Key 추출
            // GPT Lambda 호출 결과 파싱
            JsonNode gptJson = objectMapper.readTree(gptResponseJson);
            JsonNode bodyJson = objectMapper.readTree(gptJson.get("body").asText());  // <- 여기 한 번 더 파싱
            String formattedResultS3Key = bodyJson.get("formattedResultS3Key").asText();
            log.info("📁 포맷된 결과 S3 Key: {}", formattedResultS3Key);

            // 5. S3에서 해당 JSON 파일 다운로드
            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket(RESULT_BUCKET)
                    .key(formattedResultS3Key)
                    .build();

            log.info("⬇️ S3에서 포맷 결과 JSON 다운로드 시작");

            try (InputStream inputStream = s3Client.getObject(getObjectRequest)) {
                String jsonText = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))
                        .lines()
                        .collect(Collectors.joining("\n"));

                log.info("📦 다운로드된 JSON 내용: {}", jsonText);

                JsonNode parsedJson = objectMapper.readTree(jsonText);
                String formattedText = parsedJson.get("formattedText").asText();

                log.info("✅ 최종 포맷된 텍스트: {}", formattedText);

                // 이후 DB 저장 로직 등 수행
                saveRecordVerification(userId,s3ImageUrl,formattedText);

            }

        } catch (Exception e) {
            log.error("🚨 기록 검증 중 오류 발생", e);
            throw new RuntimeException("기록 검증 실패", e);
        }
    }

    public void saveRecordVerification(Long userId,String s3ImageUrl,String formattedText){
        try {
            RecordInfo info = objectMapper.readValue(formattedText, RecordInfo.class);

            // 예시 출력
            System.out.println(info.getMarathonName());
            System.out.println(info.getRunningType());
            System.out.println(info.getRecord());

            User user = userRepository.getReferenceById(userId);

            RecordVerification recordVerification = RecordVerification.builder()
                    .user(user)
                    .imageUrl(s3ImageUrl)
                    .marathonName(info.getMarathonName())
                    .runningType(RunningType.valueOf(info.getRunningType()))
                    .recordTime(convertToSeconds(info.getRecord()))
                    .status(VerificationStatus.PENDING)
                    .build();

            recordVerificationRepository.save(recordVerification);
        } catch (Exception e) {
            log.error("JSON 파싱 실패", e);
            throw new RuntimeException("기록 파싱 실패", e);
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
