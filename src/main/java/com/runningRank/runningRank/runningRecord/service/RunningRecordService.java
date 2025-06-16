package com.runningRank.runningRank.runningRecord.service;

import com.runningRank.runningRank.runningRecord.domain.RunningRecord;
import com.runningRank.runningRank.runningRecord.domain.RunningType;
import com.runningRank.runningRank.runningRecord.dto.OverallRunningRankDto;
import com.runningRank.runningRank.runningRecord.dto.RunningRankDto;
import com.runningRank.runningRank.runningRecord.dto.SchoolTopRankDto;
import com.runningRank.runningRank.runningRecord.dto.SimpleUserDto;
import com.runningRank.runningRank.runningRecord.repository.RunningRecordRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@RequiredArgsConstructor
@Slf4j
public class RunningRecordService {

    private final RunningRecordRepository runningRecordRepository;

    /**
     * 학교별 종목 기록 랭킹 조회
     */
    public List<SchoolTopRankDto> getRankingsBySchoolAndType(String universityName, RunningType runningType) {

        List<RunningRecord> records = runningRecordRepository.findRankingBySchoolAndType(universityName, runningType.name());

        AtomicInteger rankCounter = new AtomicInteger(1);

        return records.stream()
                .map(record -> SchoolTopRankDto.builder()
                        .rank(rankCounter.getAndIncrement())
                        .type(record.getRunningType())
                        .recordTimeInSeconds(record.getRecordTimeInSeconds())
                        .recordDate(record.getRecordDate())
                        .user(new SimpleUserDto(
                                record.getUser().getId(),
                                record.getUser().getName(),
                                record.getUser().getEmail(),
                                record.getUser().getUniversity().getUniversityName(),
                                record.getUser().getStudentNumber(),
                                record.getUser().getProfileImageUrl()
                        ))
                        .build()
                )
                .toList();
    }

    /**
     * 통합 종목 기록 랭킹 조회
     */
    public List<OverallRunningRankDto> getTopRankingsByType(RunningType type) {
        List<RunningRecord> records = runningRecordRepository.findTop100ByTypeOrderByRecordTimeAsc(type.name());

        AtomicInteger rankCounter = new AtomicInteger(1);

        return records.stream()
                .map(record -> OverallRunningRankDto.builder()
                        .rank(rankCounter.getAndIncrement())
                        .type(record.getRunningType())
                        .recordTimeInSeconds(record.getRecordTimeInSeconds())
                        .recordDate(record.getRecordDate())
                        .user(new SimpleUserDto(
                                record.getUser().getId(),
                                record.getUser().getName(),
                                record.getUser().getEmail(),
                                record.getUser().getUniversity().getUniversityName(),
                                record.getUser().getStudentNumber(),
                                record.getUser().getProfileImageUrl()
                        ))
                        .build()
                )
                .toList();
    }


}
