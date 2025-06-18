package com.runningRank.runningRank.runningRecord.service;

import com.runningRank.runningRank.runningRecord.domain.RunningRecord;
import com.runningRank.runningRank.runningRecord.domain.RunningType;
import com.runningRank.runningRank.runningRecord.dto.OverallRunningRankDto;
import com.runningRank.runningRank.runningRecord.dto.SimpleUserDto;
import com.runningRank.runningRank.runningRecord.repository.RunningRecordRepository;
import io.micrometer.common.lang.Nullable;
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
     * 학교별 종목 기록 랭킹 조회 (성별 필터는 선택적)
     */
    public List<OverallRunningRankDto> getRankingsBySchoolAndType(String universityName, RunningType runningType, @Nullable String gender) {
        List<RunningRecord> records;

        if (gender.equals("")) {
            records = runningRecordRepository.findRankingBySchoolAndType(universityName, runningType.name());
        } else if ("MALE".equalsIgnoreCase(gender)) {
            records = runningRecordRepository.findRankingBySchoolAndTypeAndMale(universityName, runningType.name());
        } else if ("FEMALE".equalsIgnoreCase(gender)) {
            records = runningRecordRepository.findRankingBySchoolAndTypeAndFemale(universityName, runningType.name());
        } else {
            throw new IllegalArgumentException("성별은 'MALE', 'FEMALE' 또는 null 이어야 합니다.");
        }

        AtomicInteger rankCounter = new AtomicInteger(1);

        return records.stream()
                .map(record -> OverallRunningRankDto.builder()
                        .rank(rankCounter.getAndIncrement())
                        .type(record.getRunningType())
                        .marathonName(record.getMarathonName())
                        .recordTimeInSeconds(record.getRecordTimeInSeconds())
                        .user(new SimpleUserDto(
                                record.getUser().getId(),
                                record.getUser().getName(),
                                record.getUser().getEmail(),
                                String.valueOf(record.getUser().getGender()),
                                record.getUser().getUniversity().getUniversityName(),
                                record.getUser().getStudentNumber(),
                                record.getUser().getProfileImageUrl()
                        ))
                        .build())
                .toList();
    }

    /**
     * 통합 종목 기록 랭킹 조회
     */
    public List<OverallRunningRankDto> getTopRankingsByType(RunningType type, @Nullable String gender) {
        List<RunningRecord> records;
        if (gender.equals("")) {
            records = runningRecordRepository.findTop100ByTypeOrderByRecordTimeAsc(type.name());
        } else if ("MALE".equalsIgnoreCase(gender)) {
            records = runningRecordRepository.findTop100ByTypeOrderByRecordTimeAscAndMale(type.name());
        } else if ("FEMALE".equalsIgnoreCase(gender)) {
            records = runningRecordRepository.findTop100ByTypeOrderByRecordTimeAscAndFemale(type.name());
        } else {
            throw new IllegalArgumentException("성별은 'MALE', 'FEMALE' 또는 null 이어야 합니다.");
        }
        AtomicInteger rankCounter = new AtomicInteger(1);

        return records.stream()
                .map(record -> OverallRunningRankDto.builder()
                        .rank(rankCounter.getAndIncrement())
                        .type(record.getRunningType())
                        .marathonName(record.getMarathonName())
                        .recordTimeInSeconds(record.getRecordTimeInSeconds())
                        .user(new SimpleUserDto(
                                record.getUser().getId(),
                                record.getUser().getName(),
                                record.getUser().getEmail(),
                                String.valueOf(record.getUser().getGender()), // ✅ 수정된 부분
                                record.getUser().getUniversity().getUniversityName(),
                                record.getUser().getStudentNumber(),
                                record.getUser().getProfileImageUrl()
                        ))
                        .build()
                )
                .toList();
    }
}
