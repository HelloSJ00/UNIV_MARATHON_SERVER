package com.runningRank.runningRank.runningRecord.service;

import com.runningRank.runningRank.runningRecord.domain.RunningType;
import com.runningRank.runningRank.runningRecord.dto.RunningRankDto;
import com.runningRank.runningRank.runningRecord.repository.RunningRecordRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class RunningRecordCacheService {

    private final RunningRecordRepository runningRecordRepository;
    /**
     * 상위 100명 랭킹을 조회하고 캐싱하는 내부 헬퍼 메소드.
     * 이 메소드의 결과는 캐시됩니다.
     */
    @Cacheable(value = "top100RankingsCache",
            key = "#uniName + '_' + #runningType.name() + '_' + #gender + '_' + #graduationStatus")
    public List<RunningRankDto> getTop100RankingsInternal(
            String uniName,
            RunningType runningType,
            String gender,
            String graduationStatus
    ) {
        log.info("[캐시 미스 또는 새로고침] DB에서 상위 100명 랭킹 조회 중... 종목: {}, 성별: {}, 학교: {}, 졸업여부: {}",
                runningType, gender, uniName != null ? uniName : "전체", graduationStatus);

        // 실제 DB 조회 로직
        List<RunningRankDto> records = runningRecordRepository.getTop100Rankings(
                runningType.name(),
                uniName,
                gender,
                graduationStatus
        );
        log.debug("[DB 조회 완료] 상위 100명 랭킹 {}개 로드됨.", records.size());

        return records;
    }
}
