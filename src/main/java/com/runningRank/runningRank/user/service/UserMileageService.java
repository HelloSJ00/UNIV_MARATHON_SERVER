package com.runningRank.runningRank.user.service;

import com.runningRank.runningRank.user.domain.User;
import com.runningRank.runningRank.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class UserMileageService {

    private final UserRepository userRepository;

    // 람다가 토큰 갱신 후 호출할 메서드
    @Transactional
    public void updateUserStravaTokens(Long userId, String newAccessToken, String newRefreshToken, LocalDateTime newExpiresAt) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + userId));

        user.updateStravaTokens(newAccessToken, newRefreshToken, newExpiresAt);

        // lastUpdatedAt은 @EntityListeners나 DB 기능으로 자동 업데이트될 수 있음
        userRepository.save(user);
        System.out.println("User " + userId + "의 Strava 토큰 정보가 갱신되었습니다.");
    }
}

