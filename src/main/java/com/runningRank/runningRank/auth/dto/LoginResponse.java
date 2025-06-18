package com.runningRank.runningRank.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import com.runningRank.runningRank.auth.dto.UserInfo;

@Getter
@Builder
@AllArgsConstructor
public class LoginResponse {
    private String accessToken;
    private String tokenType; // "Bearer"
    private UserInfo user; // 유저 정보 + 러닝 기록 포함된 DTO
}
