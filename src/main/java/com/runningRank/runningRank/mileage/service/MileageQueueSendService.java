package com.runningRank.runningRank.mileage.service;

import com.runningRank.runningRank.messaging.MileageSqsProducer;
import com.runningRank.runningRank.mileage.repository.MileageRepository;
import com.runningRank.runningRank.strava.config.StravaApiConfig;
import com.runningRank.runningRank.strava.dto.StravaTokenResponse;
import com.runningRank.runningRank.user.domain.User;
import com.runningRank.runningRank.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class MileageQueueSendService {

    private final StravaApiConfig stravaApiConfig;
    private final RestTemplate restTemplate;
    private final UserRepository userRepository;
    private final MileageSqsProducer mileageSqsProducer;

    /**
     * 매일 자정마다 Strava 연동된 유저의 userId와 accessToken을 SQS로 전송한다.
     */
    @Scheduled(cron = "0 0 0 * * *", zone = "Asia/Seoul") // 매일 자정
    public void sendMileageToQueue() {
        log.info("⏰ Strava 연동 유저 대상 SQS 전송 작업 시작");

        List<User> stravaUsers = userRepository.findAllByIsStravaConnectedTrue();

        for (User user : stravaUsers) {
            // accessToken 유효성 검사
            if (user.isAccessTokenExpired()) {
                log.info("🔁 유저 {}의 Strava accessToken이 만료됨. refresh 시도", user.getId());
                try {
                    refreshAccessToken(user); // ↓ 아래에 정의함
                } catch (Exception e) {
                    log.error("❌ 유저 {}의 Strava accessToken refresh 실패. 전송 생략", user.getId(), e);
                    continue;
                }
            }

            // accessToken 전송
            String accessToken = user.getStravaAccessToken();
            if (accessToken == null || accessToken.isEmpty()) {
                log.warn("🚫 유저 {}는 accessToken이 없어 전송 생략", user.getId());
                continue;
            }

            mileageSqsProducer.sendMileageJob(user.getId(), accessToken);
        }

        log.info("✅ Strava 연동 유저 SQS 전송 완료: 총 {}명", stravaUsers.size());
    }

    private void refreshAccessToken(User user) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("client_id", stravaApiConfig.getClientId());
        params.add("client_secret", stravaApiConfig.getClientSecret());
        params.add("grant_type", "refresh_token");
        params.add("refresh_token", user.getStravaRefreshToken());

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);

        ResponseEntity<StravaTokenResponse> response = restTemplate.postForEntity(
                StravaApiConfig.TOKEN_URL,
                request,
                StravaTokenResponse.class
        );

        if (response.getStatusCode() == HttpStatus.OK) {
            StravaTokenResponse tokenInfo = response.getBody();

            String newAccessToken = tokenInfo.getAccessToken();
            String newRefreshToken = tokenInfo.getRefreshToken();
            long expiresAtUnix = tokenInfo.getExpiresAt();
            LocalDateTime expiresAt = LocalDateTime.ofEpochSecond(expiresAtUnix, 0, ZoneOffset.UTC);

            user.updateStravaTokens(newAccessToken, newRefreshToken, expiresAt);
            userRepository.save(user);

            log.info("✅ 유저 {}의 Strava accessToken 갱신 완료", user.getId());
        } else {
            throw new RuntimeException("Strava 토큰 갱신 실패: " + response.getStatusCode());
        }
    }
}
