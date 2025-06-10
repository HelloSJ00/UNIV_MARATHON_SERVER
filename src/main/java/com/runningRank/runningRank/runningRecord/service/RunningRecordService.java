package com.runningRank.runningRank.runningRecord.service;

import com.runningRank.runningRank.runningRecord.domain.RunningRecord;
import com.runningRank.runningRank.runningRecord.domain.RunningType;
import com.runningRank.runningRank.runningRecord.repository.RunningRecordRepository;
import com.runningRank.runningRank.user.domain.School;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class RunningRecordService {

    private final RunningRecordRepository runningRecordRepository;

    /**
     * 학교별 종목 기록 랭킹 조회
     */
    public List<RunningRecord> getRankingsBySchoolAndType(School school, RunningType type) {
        return runningRecordRepository.findRankingBySchoolAndType(school.name(), type.name());
    }

    /**
     * 통합 종목 기록 랭킹 조회
     */
    public List<RunningRecord> getTop100RankingByType(RunningType type) {
        return runningRecordRepository.findTop100ByTypeOrderByRecordTimeAsc(type.name());
    }

}
