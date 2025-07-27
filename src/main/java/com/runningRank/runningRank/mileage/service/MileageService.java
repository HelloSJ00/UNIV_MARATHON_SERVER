package com.runningRank.runningRank.mileage.service;

import com.runningRank.runningRank.messaging.MileageSqsProducer;
import com.runningRank.runningRank.mileage.domain.Mileage;
import com.runningRank.runningRank.mileage.dto.MileageUpdateResponse;
import com.runningRank.runningRank.mileage.repository.MileageRepository;
import com.runningRank.runningRank.user.domain.User;
import com.runningRank.runningRank.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class MileageService {

    private final MileageRepository mileageRepository;
    private final UserRepository userRepository;


    // 람다가 계산한 월별 마일리지 정보를 수신하여 저장/업데이트
    @Transactional
    public void saveOrUpdateMonthlyMileage(
            MileageUpdateResponse mileageUpdateResponse
    ) {
        log.info("📥 마일리지 저장 요청 도착: userId={}, year={}, month={}",
                mileageUpdateResponse.getUserId(),
                mileageUpdateResponse.getYear(),
                mileageUpdateResponse.getMonth());

        User user = userRepository.findById(mileageUpdateResponse.getUserId())
                .orElseThrow(() -> {
                    log.error("❌ 사용자 조회 실패: userId={}", mileageUpdateResponse.getUserId());
                    return new IllegalArgumentException("User not found with ID: " + mileageUpdateResponse.getUserId());
                });

        log.info("✅ 사용자 조회 성공: userId={}, name={}", user.getId(), user.getName());

        Optional<Mileage> existingMileage = mileageRepository.findByUserAndYearAndMonth(
                user,
                mileageUpdateResponse.getYear(),
                mileageUpdateResponse.getMonth()
        );

        Mileage mileage;
        if (existingMileage.isPresent()) {
            mileage = existingMileage.get();
            log.info("🔁 기존 마일리지 레코드 존재. 람다 구하기");
            mileage.updateTotalDistanceKm(
                    mileageUpdateResponse.getTotalActivityCount(),
                    mileageUpdateResponse.getTotalDistanceKm(),
                    mileageUpdateResponse.getAvgPaceTime()
            );
        } else {
            log.info("🆕 새로운 마일리지 레코드 생성");
            mileage = Mileage.of(
                    user,
                    mileageUpdateResponse.getYear(),
                    mileageUpdateResponse.getMonth(),
                    mileageUpdateResponse.getTotalActivityCount(),
                    mileageUpdateResponse.getTotalDistanceKm(),
                    mileageUpdateResponse.getAvgPaceTime()
            );
        }

        mileageRepository.save(mileage);
    }
}
