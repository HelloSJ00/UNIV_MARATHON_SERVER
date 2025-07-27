package com.runningRank.runningRank.strava.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.runningRank.runningRank.global.util.StateTokenUtil;
import com.runningRank.runningRank.strava.config.StravaApiConfig;
import com.runningRank.runningRank.user.domain.User;
import com.runningRank.runningRank.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;


import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class StravaAuthService {
    private final StravaApiConfig stravaApiConfig;
    private final UserRepository userRepository;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final StateTokenUtil stateTokenUtil;

    /**
     * Strava OAuth 인증을 위한 URL을 생성하여 반환합니다.
     * 프론트엔드에서 이 URL로 리디렉션하여 사용자가 Strava 인증 페이지로 이동하게 합니다.
     * state 파라미터는 JWT 형태로 현재 사용자 ID와 랜덤 논스(nonce)를 포함합니다.
     *
     * @param currentLoggedInUserId 현재 로그인한 사용자의 고유 ID
     * @return Strava 인증 페이지로 리다이렉션할 URL
     */
    public String generateStravaAuthUrl(Long currentLoggedInUserId) {
        // StateTokenUtil을 사용하여 userId를 포함하는 JWT 형태의 state 토큰을 생성합니다.
        String state = stateTokenUtil.generateStateToken(currentLoggedInUserId);
        return UriComponentsBuilder.fromUriString(StravaApiConfig.AUTHORIZE_URL)
                .queryParam("client_id", stravaApiConfig.getClientId())
                .queryParam("response_type", "code")
                .queryParam("redirect_uri", stravaApiConfig.getRedirectUri())
                .queryParam("approval_prompt", "auto") // 'auto'는 이미 승인된 경우 다시 묻지 않음, 'force'는 항상 다시 물음
                .queryParam("scope", StravaApiConfig.SCOPE) // 필요한 권한 스코프
                .queryParam("state", state) // JWT 형태의 state 토큰 포함
                .toUriString();
    }

    /**
     * Strava로부터 받은 권한 부여 코드(authorization code)를 사용하여
     * Access Token, Refresh Token을 교환하고 사용자 정보를 저장/업데이트합니다.
     */
    @Transactional
    public User exchangeCodeForTokens(String authorizationCode, String stateFromFrontend) {
        // 1. state 토큰 검증 및 userId 추출
        Map<String, Object> claims = stateTokenUtil.validateStateToken(stateFromFrontend);
        Long currentLoggedInUserId = ((Integer) claims.get("userId")).longValue();

        // 현재 로그인된 사용자를 DB에서 조회 (이 사용자에게 Strava 정보를 연결할 것임)
        User user = userRepository.findById(currentLoggedInUserId)
                .orElseThrow(() -> new IllegalArgumentException("현재 로그인된 사용자를 찾을 수 없습니다. ID: " + currentLoggedInUserId));

        // 2. Strava 토큰 교환 API 요청
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("client_id", stravaApiConfig.getClientId());
        params.add("client_secret", stravaApiConfig.getClientSecret());
        params.add("code", authorizationCode);
        params.add("grant_type", "authorization_code");

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(StravaApiConfig.TOKEN_URL, request, String.class);

            if (response.getStatusCode() == HttpStatus.OK) {
                JsonNode root = objectMapper.readTree(response.getBody());

                String accessToken = root.path("access_token").asText();
                String refreshToken = root.path("refresh_token").asText();
                long expiresAtUnix = root.path("expires_at").asLong(); // Unix Timestamp
                JsonNode athleteNode = root.path("athlete");
                String stravaId = athleteNode.path("id").asText();
                String username = athleteNode.path("username").asText();
                String email = athleteNode.path("email").asText(); // 이메일 스코프를 요청했다면

                // Unix Timestamp를 LocalDateTime으로 변환 (UTC 기준)
                LocalDateTime expiresAt = LocalDateTime.ofEpochSecond(expiresAtUnix, 0, ZoneOffset.UTC);

                user.updateStravaTokens(accessToken, refreshToken, expiresAt);

                return userRepository.save(user); // DB에 저장/업데이트
            } else {
                throw new RuntimeException("Strava token exchange failed with status: " + response.getStatusCode() + " body: " + response.getBody());
            }
        } catch (Exception e) {
            System.err.println("Strava 토큰 교환 중 오류 발생: " + e.getMessage());
            throw new RuntimeException("Failed to exchange Strava code for tokens.", e);
        }
    }
}
