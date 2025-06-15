package com.runningRank.runningRank.recordVerification.service;

import org.springframework.stereotype.Component;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.lambda.LambdaClient;
import software.amazon.awssdk.services.lambda.model.InvokeRequest;
import software.amazon.awssdk.services.lambda.model.InvokeResponse;

/**
 * 람다 호출을 위한 컴포넌트
 * 1. OCR -> 기록증에서 필요한 텍스트 추출
 * 2. GPT4 -> 추출된 텍스트를 동일한 형태로 가공
 */
@Component
public class RecordVerificationLambdaClient {
    private final LambdaClient lambdaClient;

    /**
     * aws 설정
     * 서울 리전으로 설정됨
     */
    public RecordVerificationLambdaClient() {
        this.lambdaClient = LambdaClient.builder()
                .region(Region.AP_NORTHEAST_2)
                .build();
    }

    /**
     *
     * @param s3ImageUrl
     * @return
     */
    public String callRecordVerification(String s3ImageUrl) {
        String payload = String.format("{\"s3ImageUrl\": \"%s\"}", s3ImageUrl);

        InvokeRequest request = InvokeRequest.builder()
                .functionName("record-verification-handler")
                .payload(SdkBytes.fromUtf8String(payload))
                .build();

        InvokeResponse response = lambdaClient.invoke(request);
        return response.payload().asUtf8String();
    }

    /**
     * 추출된 텍스트를 원하는 형태로 가공하는로직
     * callRecordVerification 에서 반환된 s3url을 파라미터로 aws lambda를 호출하면
     * 원하는 형태로 텍스트를 가공한 s3url이 반환됨
     * @param ocrResultS3Key
     * @return
     */
    public String callGptFormattingLambda(String ocrResultS3Key) {
        String payload = String.format("{\"s3Key\": \"%s\"}", ocrResultS3Key);

        InvokeRequest request = InvokeRequest.builder()
                .functionName("record-formatting-handler") // GPT 람다
                .payload(SdkBytes.fromUtf8String(payload))
                .build();

        InvokeResponse response = lambdaClient.invoke(request);
        return response.payload().asUtf8String(); // {"formattedText": "..."}
    }
}
}
