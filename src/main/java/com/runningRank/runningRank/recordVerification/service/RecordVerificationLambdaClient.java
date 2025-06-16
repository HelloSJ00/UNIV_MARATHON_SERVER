package com.runningRank.runningRank.recordVerification.service;

import lombok.RequiredArgsConstructor;
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
@RequiredArgsConstructor
public class RecordVerificationLambdaClient {
    private final LambdaClient lambdaClient;

    /**
     * 기록증 저장된 s3url을 파라미터로 람다를 호출하면 기록증 텍스트를 추출하고 s3에 저장
     * 저장된 s3url 반환
     * @param s3ImageUrl
     * @return
     */
    public String callGoogleVisionOCR(String s3ImageUrl) {
        String payload = String.format("{\"s3_url\": \"%s\"}", s3ImageUrl);

        InvokeRequest request = InvokeRequest.builder()
                .functionName("univ-marathon-record-ocr")
                .payload(SdkBytes.fromUtf8String(payload))
                .build();

        InvokeResponse response = lambdaClient.invoke(request);
        return response.payload().asUtf8String();
    }

    /**
     * 추출된 텍스트를 원하는 형태로 가공하는로직
     * callRecordVerification 에서 반환된 s3url을 파라미터로 aws lambda를 호출하면
     * 원하는 형태로 텍스트를 가공한 s3url이 반환됨
     * Response
     * {
     *     "이름" : "오승준"
     *     "종목" : "10K"
     *     "기록" : "00:39:03"
     *     "참가 대회" : "대전서구청장배마라톤"
     *     "createdAt" : 2025/05/18
     * }
     * @param ocrResultS3Key
     * @return
     */
    public String callGptFormattingLambda(String ocrResultS3Key) {
        String payload = String.format("{\"s3_url\": \"%s\"}", ocrResultS3Key);

        InvokeRequest request = InvokeRequest.builder()
                .functionName("univ-marathon-ocr-to-json") // GPT 람다
                .payload(SdkBytes.fromUtf8String(payload))
                .build();

        InvokeResponse response = lambdaClient.invoke(request);
        return response.payload().asUtf8String(); // {"formattedText": "..."}
    }
}
