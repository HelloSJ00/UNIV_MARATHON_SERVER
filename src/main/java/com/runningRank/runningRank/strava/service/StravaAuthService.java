package com.runningRank.runningRank.strava.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.runningRank.runningRank.global.util.StateTokenUtil;
import com.runningRank.runningRank.strava.config.StravaApiConfig;
import com.runningRank.runningRank.strava.dto.StravaTokenResponse;
import com.runningRank.runningRank.user.domain.User;
import com.runningRank.runningRank.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
public class StravaAuthService {
    private final StravaApiConfig stravaApiConfig;
    private final UserRepository userRepository;
    private final RestTemplate restTemplate;
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
        // 1. 사용자 ID 확인
        log.info("🧑 현재 로그인한 사용자 ID: {}", currentLoggedInUserId);

        // 2. JWT 형태의 state 생성
        String state = stateTokenUtil.generateStateToken(currentLoggedInUserId);
        log.info("🔐 생성된 JWT state 토큰: {}", state);

        // 3. URL 생성
        String redirectUrl = UriComponentsBuilder.fromUriString(StravaApiConfig.AUTHORIZE_URL)
                .queryParam("client_id", stravaApiConfig.getClientId())
                .queryParam("response_type", "code")
                .queryParam("redirect_uri", stravaApiConfig.getRedirectUri())
                .queryParam("approval_prompt", "auto")
                .queryParam("scope", StravaApiConfig.SCOPE)
                .queryParam("state", state)
                .toUriString();

        log.info("🔗 최종 Strava 인증 URL: {}", redirectUrl);
        return redirectUrl;
    }

    /**
     * Strava로부터 받은 권한 부여 코드(authorization code)를 사용하여
     * Access Token, Refresh Token을 교환하고 사용자 정보를 저장/업데이트합니다.
     */
    @Transactional
    public User exchangeCodeForTokens(String authorizationCode, String stateFromFrontend) {
        log.info("🔁 Strava 토큰 교환 시작 - code: {}, state: {}", authorizationCode, stateFromFrontend);

        // 1. state 토큰 검증 및 userId 추출
        Map<String, Object> claims = stateTokenUtil.validateStateToken(stateFromFrontend);
        Long currentLoggedInUserId = ((Integer) claims.get("userId")).longValue();
        log.info("✅ JWT state 토큰 검증 완료 - userId: {}", currentLoggedInUserId);

        User user = userRepository.findById(currentLoggedInUserId)
                .orElseThrow(() -> {
                    log.error("❌ 유저 ID {} 에 해당하는 유저를 찾을 수 없음", currentLoggedInUserId);
                    return new IllegalArgumentException("현재 로그인된 사용자를 찾을 수 없습니다. ID: " + currentLoggedInUserId);
                });

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
            log.info("🌐 Strava 토큰 요청 전송 중...");
            ResponseEntity<StravaTokenResponse> response = restTemplate.postForEntity(
                    StravaApiConfig.TOKEN_URL,
                    request,
                    StravaTokenResponse.class
            );

            if (response.getStatusCode() == HttpStatus.OK) {
                StravaTokenResponse tokenInfo = response.getBody();

                String accessToken = tokenInfo.getAccessToken();
                String refreshToken = tokenInfo.getRefreshToken();
                long expiresAtUnix = tokenInfo.getExpiresAt();
                LocalDateTime expiresAt = LocalDateTime.ofEpochSecond(expiresAtUnix, 0, ZoneOffset.UTC);

                log.info("✅ Strava 토큰 수신 성공 - accessToken: {}, refreshToken: {}, expiresAt: {}", accessToken, refreshToken, expiresAt);

                user.updateStravaTokens(accessToken, refreshToken, expiresAt);
                userRepository.save(user);

                log.info("💾 사용자 Strava 정보 업데이트 완료 - userId: {}", user.getId());
                return user;

            } else {
                log.error("❌ Strava 토큰 요청 실패 - status: {}, body: {}", response.getStatusCode(), response.getBody());
                throw new RuntimeException("Strava token exchange failed with status: " + response.getStatusCode());
            }
        } catch (Exception e) {
            log.error("🔥 Strava 토큰 교환 중 예외 발생: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to exchange Strava code for tokens.", e);
        }
    }
}
