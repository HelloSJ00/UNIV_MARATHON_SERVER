package com.runningRank.runningRank.mileage.dto;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
public class StravaTokenUpdateRequest {
    private Long userId; // 스프링 앱 내부의 User ID (DB PK)
    private String newAccessToken;
    private String newRefreshToken;
    private LocalDateTime newExpiresAt; // UTC 기준 시간일 가능성이 높으므로 파싱 주의
}
