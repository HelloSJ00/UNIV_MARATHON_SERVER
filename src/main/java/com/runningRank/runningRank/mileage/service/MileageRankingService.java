package com.runningRank.runningRank.mileage.service;

import com.runningRank.runningRank.mileage.dto.MileageRankingResponse;
import com.runningRank.runningRank.mileage.dto.MileageUnivRankDto;
import com.runningRank.runningRank.mileage.dto.MyMileageRankInfo;
import com.runningRank.runningRank.mileage.repository.MileageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@Slf4j
@RequiredArgsConstructor
public class MileageRankingService {

    private final MileageRepository mileageRepository;

    public MileageRankingResponse getTop100MileageRankingsWithMyRecord(
            Long userId,
            String universityName,
            String gender,
            String graduationStatus
    ) {
        List<MileageUnivRankDto> rankings;
        MyMileageRankInfo myrecord= null;


        try{
            // 1. 현재 날짜 가져오기
            LocalDate now = LocalDate.now();

            // 2. 현재 년도와 달 추출
            int currentYear = now.getYear();       // 예: 2025
            int currentMonth = now.getMonthValue(); // 예: 7 (JULY는 7)
            rankings = mileageRepository.getTop100MileageRankings(currentMonth,currentYear,universityName,gender,graduationStatus);

            // 랭킹 순위 부여 (캐시된 데이터에도 순위를 다시 부여해야 할 수 있습니다.
            // 만약 순위가 DB에서 이미 부여되어 온다면 이 부분은 불필요할 수 있습니다.)
            AtomicInteger rankCounter = new AtomicInteger(1);
            rankings.forEach(record -> record.setRank(rankCounter.getAndIncrement()));

            // 3. 현재 유저의 랭킹 정보 가져오기
            // 2. 현재 유저의 랭킹 정보 조회 (캐싱하지 않음)
            myrecord = mileageRepository.findMyMileageRankInfo(
                    currentYear,
                    currentMonth,
                    userId,
                    gender,
                    universityName,
                    graduationStatus
            ).orElse(null);
        }catch (Exception e){
            log.error("MileageRankingService.getTop100MileageRankingsWithMyRecord error",e);
            return null;
        }

        log.info("[랭킹 조회 완료] 최종 응답 객체 생성. 상위 {}명, 내 기록 {}존재",
                rankings.size(), myrecord != null ? "정상" : "없음");

        return new MileageRankingResponse(rankings,myrecord);
    }
}
