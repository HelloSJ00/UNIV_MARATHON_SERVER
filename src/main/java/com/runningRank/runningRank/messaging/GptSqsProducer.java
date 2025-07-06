package com.runningRank.runningRank.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.runningRank.runningRank.messaging.dto.GptJobMessage;
import com.runningRank.runningRank.messaging.dto.OcrJobMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;

import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class GptSqsProducer {

    private final SqsClient sqsClient;
    private final ObjectMapper objectMapper;

    @Value("${cloud.aws.sqs.gpt-queue-url}")
    private String gptQueueUrl;

    public void sendGptJob(Long userId,UUID jobId, String s3ImageUrl,String s3TextUrl) {
        try {
            GptJobMessage message = new GptJobMessage(userId, jobId, s3ImageUrl,s3TextUrl);
            String body = objectMapper.writeValueAsString(message);

            sqsClient.sendMessage(SendMessageRequest.builder()
                    .queueUrl(gptQueueUrl)
                    .messageBody(body)
                    .build());

            log.info("📤 Gpt SQS 메시지 전송 완료: {}", body);
        } catch (Exception e) {
            log.error("❌ Gpt SQS 전송 실패", e);
            throw new RuntimeException("SQS 전송 실패", e);
        }
    }
}
