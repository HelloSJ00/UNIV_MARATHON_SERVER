package com.runningRank.runningRank.auth.dto;

import com.runningRank.runningRank.user.domain.Role;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class KakaoUserInfo {
    private String oauthId;               // 카카오 고유 사용자 ID (sub 역할)
    private String nickname;             // 닉네임
    private String profileImageUrl;      // 프로필 이미지 URL
}
