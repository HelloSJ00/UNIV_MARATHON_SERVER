package com.runningRank.runningRank.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.runningRank.runningRank.messaging.dto.MileageJobMessage;
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
public class MileageSqsProducer {
    private final SqsClient sqsClient;
    private final ObjectMapper objectMapper;

    @Value("${cloud.aws.sqs.mileage-queue-url}")
    private String mileageQueueUrl;

    public void sendMileageJob(Long userId,String accessToken) {
        try {
            MileageJobMessage message = new MileageJobMessage(userId,accessToken);
            String body = objectMapper.writeValueAsString(message);

            sqsClient.sendMessage(SendMessageRequest.builder()
                    .queueUrl(mileageQueueUrl)
                    .messageBody(body)
                    .build());

            log.info("📤 Mileage SQS 메시지 전송 완료: {}", body);
        } catch (Exception e) {
            log.error("❌ Mileage SQS 전송 실패", e);
            throw new RuntimeException("SQS 전송 실패", e);
        }
    }
}
