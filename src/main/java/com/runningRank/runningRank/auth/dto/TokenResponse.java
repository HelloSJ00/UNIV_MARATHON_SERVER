package com.runningRank.runningRank.auth.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
@AllArgsConstructor
public class TokenResponse {
    private String accessToken;
//    private String refreshToken;
    private String tokenType; // "Bearer"
}
