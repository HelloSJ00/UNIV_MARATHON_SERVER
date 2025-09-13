package com.runningRank.runningRank.recordVerification.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.runningRank.runningRank.recordVerification.dto.RecordInfo;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;

@Slf4j
public class RecordVerificationUtil {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static int convertToSeconds(String hhmmss) {
        String[] parts = hhmmss.split(":");
        int hour = Integer.parseInt(parts[0]);
        int minute = Integer.parseInt(parts[1]);
        int second = Integer.parseInt(parts[2]);
        return hour * 3600 + minute * 60 + second;
    }

    public static JsonNode parseJson(InputStream inputStream) {
        try {
            return new ObjectMapper().readTree(inputStream); // 또는 외부에서 ObjectMapper 주입
        } catch (IOException e) {
            throw new RuntimeException("JSON 파싱 실패", e);
        }
    }

    public static String extractFormattedText(JsonNode jsonNode, String key) {
        JsonNode node = jsonNode.get("formattedText");
        if (node == null || node.isMissingNode() || node.isNull()) {
            throw new IllegalArgumentException("formattedText 필드 없음. key: " + key);
        }
        return node.asText();
    }

    public static RecordInfo parseRecordInfo(String formattedText) {
        try {
            return objectMapper.readValue(formattedText, RecordInfo.class);
        } catch (IOException e) {
            throw new IllegalArgumentException("RecordInfo 파싱 실패", e);
        }
    }

    public static RecordInfo parseRecordInfoOrThrow(String formattedText) {
        try {
            return RecordVerificationUtil.parseRecordInfo(formattedText);
        } catch (Exception e) {
            log.error("🚨 기록 파싱 실패: {}", formattedText, e);
            throw new RuntimeException("기록 파싱 실패", e);
        }
    }
}
