package com.runningRank.runningRank.universityRanking.service;

import com.runningRank.runningRank.runningRecord.repository.RunningRecordRepository;
import com.runningRank.runningRank.universityRanking.dto.FinisherUnivRanking;
import com.runningRank.runningRank.universityRanking.repository.UniversityRankingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class UniversityRankingService {
    private final UniversityRankingRepository universityRankingRepository;

    public List<FinisherUnivRanking> getFinisherUniversityRankings(String runningType){
        // 1. 메서드 진입 로그 (디버그 레벨)
        // 어떤 runningType(달리기 유형)으로 랭킹 조회를 시작하는지 확인합니다.
        log.debug("getFinisherUniversityRankings 메서드 호출. runningType: {}", runningType);

        List<FinisherUnivRanking> rankings = null; // 랭킹 목록 초기화

        try {
            // 2. Repository 호출 전 로그 (정보 레벨)
            log.info("runningType: {} 에 대한 대학교 랭킹 조회를 시도합니다.", runningType);

            // 실제 Repository 메서드를 호출하여 랭킹 데이터를 가져옵니다.
            rankings = universityRankingRepository.getFinisherUniversityRankings(runningType);

            // 3. Repository 호출 후 성공 로그 (정보 레벨)
            // 조회된 랭킹의 개수를 함께 기록하여 정상 동작 여부를 확인합니다.
            log.info("runningType: {} 에 대한 대학교 랭킹 {}건을 성공적으로 조회했습니다.", runningType, rankings.size());

        } catch (Exception e) {
            // 4. 예외 발생 시 에러 로그 (에러 레벨)
            // 어떤 예외가 발생했고, 어떤 runningType 처리 중이었는지 명확하게 기록합니다.
            log.error("runningType: {} 에 대한 대학교 랭킹 조회 중 오류 발생: {}", runningType, e.getMessage(), e);
            // 원본 예외를 런타임 예외로 감싸서 다시 던져, 상위 계층에서 예외를 처리할 수 있도록 합니다.
            throw new RuntimeException("대학교 랭킹 조회에 실패했습니다.", e);
        }

        return rankings;
    }

}
