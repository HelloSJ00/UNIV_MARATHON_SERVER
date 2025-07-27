package com.runningRank.runningRank.strava.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
@Getter
public class StravaApiConfig {
    @Value("${strava.client-id}")
    private String clientId;

    @Value("${strava.client-secret}")
    private String clientSecret;

    @Value("${strava.redirect-uri}")
    private String redirectUri;

    public static final String AUTHORIZE_URL = "https://www.strava.com/oauth/authorize";
    public static final String TOKEN_URL = "https://www.strava.com/oauth/token";
    public static final String SCOPE = "activity:read_all"; // 필요한 권한: 모든 활동 데이터 읽기
}
