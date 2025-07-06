package com.runningRank.runningRank.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.runningRank.runningRank.messaging.dto.OcrJobMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;

import java.util.Map;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class OcrSqsProducer {

    private final SqsClient sqsClient;
    private final ObjectMapper objectMapper;

    @Value("${cloud.aws.sqs.ocr-queue-url}")
    private String ocrQueueUrl;

    public void sendOcrJob(Long userId, UUID jobId, String s3ImageUrl) {
        try {
            OcrJobMessage message = new OcrJobMessage(userId,jobId, s3ImageUrl);
            String body = objectMapper.writeValueAsString(message);

            sqsClient.sendMessage(SendMessageRequest.builder()
                    .queueUrl(ocrQueueUrl)
                    .messageBody(body)
                    .build());

            log.info("📤 OCR SQS 메시지 전송 완료: {}", body);
        } catch (Exception e) {
            log.error("❌ OCR SQS 전송 실패", e);
            throw new RuntimeException("SQS 전송 실패", e);
        }
    }
}