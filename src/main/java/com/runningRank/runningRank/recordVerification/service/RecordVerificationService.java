package com.runningRank.runningRank.recordVerification.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.runningRank.runningRank.recordVerification.repository.RecordVerificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class RecordVerificationService {

    private final RecordVerificationLambdaClient lambdaClient;
    private final ObjectMapper objectMapper;

    public void createRecordVerification(String s3ImageUrl) {
        try {
            // 1. OCR Lambda 호출
            String ocrResponseJson = lambdaClient.callGoogleVisionOCR(s3ImageUrl);
            log.info("✅ OCR Lambda 응답: {}", ocrResponseJson);

            // 2. OCR 결과에서 S3 Key 추출
            JsonNode ocrJson = objectMapper.readTree(ocrResponseJson);
            String ocrResultS3Key = ocrJson.get("ocrResultS3Key").asText();

            // 3. GPT Lambda 호출
            String gptResponseJson = lambdaClient.callGptFormattingLambda(ocrResultS3Key);
            log.info("✅ GPT Lambda 응답: {}", gptResponseJson);

            // 4. 가공된 텍스트 추출
            JsonNode gptJson = objectMapper.readTree(gptResponseJson);
            String formattedText = gptJson.get("formattedText").asText();

            // 5. 후처리 (예: DB 저장 등)
            log.info("✅ 최종 포맷 결과: {}", formattedText);

        } catch (Exception e) {
            log.error("🚨 기록 검증 중 오류 발생", e);
            throw new RuntimeException("기록 검증 실패", e);
        }
    }
}
