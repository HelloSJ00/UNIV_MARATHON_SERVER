package com.runningRank.runningRank.mileage.service;

import com.runningRank.runningRank.messaging.MileageSqsProducer;
import com.runningRank.runningRank.mileage.domain.Mileage;
import com.runningRank.runningRank.mileage.repository.MileageRepository;
import com.runningRank.runningRank.user.domain.User;
import com.runningRank.runningRank.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MileageService {

    private final MileageRepository mileageRepository;
    private final UserRepository userRepository;
    private final MileageSqsProducer mileageSqsProducer;


    // 람다가 계산한 월별 마일리지 정보를 수신하여 저장/업데이트
    @Transactional
    public void saveOrUpdateMonthlyMileage(
            Long userId,
            int year,
            int month,
            int totalActivityCount,
            double totalDistanceKm,
            int avgFaceTime
    ) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + userId));

        // 기존 마일리지 레코드 조회 (activityType이 있다면 조건에 추가)
        Optional<Mileage> existingMileage = mileageRepository.findByUserAndYearAndMonth(user, year, month);
        // 만약 activityType을 사용하지 않고, 월별 총합만 한다면:
        // Optional<Mileage> existingMileage = mileageRepository.findByUserAndYearAndMonth(user, year, month);

        Mileage mileage;
        if (existingMileage.isPresent()) {
            mileage = existingMileage.get();
            mileage.updateTotalDistanceKm(totalActivityCount,totalDistanceKm,avgFaceTime); // 람다에서 계산된 최종값으로 덮어쓰기
            System.out.println("기존 마일리지 업데이트: User=" + userId + ", " + year + "-" + month + " -> " + totalDistanceKm + "km");
        } else {
            mileage = Mileage.of(user,year,month,totalActivityCount,totalDistanceKm,avgFaceTime);
            System.out.println("새 마일리지 생성: User=" + userId + ", " + year + "-" + month + " -> " + totalDistanceKm + "km");
        }
        mileageRepository.save(mileage);
    }
}
