package com.runningRank.runningRank.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class KakaoLoginResponse {
    private boolean needSignUp;
    private String accessToken;
    private String tokenType; // "Bearer"
    private KakaoUserInfo kakaoUserInfo;
}

