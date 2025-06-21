package com.runningRank.runningRank.runningRecord.service;

import com.runningRank.runningRank.runningRecord.domain.RunningRecord;
import com.runningRank.runningRank.runningRecord.domain.RunningType;
import com.runningRank.runningRank.runningRecord.dto.FullRankInfo;
import com.runningRank.runningRank.runningRecord.dto.OverallRunningRankDto;
import com.runningRank.runningRank.runningRecord.dto.RunningRecordResponse;
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
     * 학교별 종목 기록 랭킹 조회 서비스
     *
     * @param userId         현재 요청한 유저의 ID
     * @param universityName 필터링할 학교 이름 (선택 사항)
     * @param runningType    조회할 마라톤 종목 (예: TEN_KM, HALF 등)
     * @param gender         필터링할 성별 (예: MALE, FEMALE, ALL)
     * @return 랭킹 리스트와 현재 유저의 개인 기록 포함된 응답 객체
     */
    public RunningRecordResponse getTop100RankingsWithMyRecord(
            Long userId,
            String universityName,
            RunningType runningType,
            String gender
    ) {
        String uniName = (universityName != null && !universityName.isBlank()) ? universityName : null;

        log.info("[랭킹 조회] 종목: {}, 성별: {}, 학교: {}", runningType, gender, uniName != null ? uniName : "전체");

        // 1. 상위 100명의 기록 조회
        List<RunningRecord> records = runningRecordRepository.getTop100Rankings(
                runningType.name(),
                uniName,
                gender
        );

        AtomicInteger rankCounter = new AtomicInteger(1);

        List<OverallRunningRankDto> ranking = records.stream()
                .map(record -> OverallRunningRankDto.builder()
                        .rank(rankCounter.getAndIncrement())
                        .type(record.getRunningType())
                        .marathonName(record.getMarathonName())
                        .recordTimeInSeconds(record.getRecordTimeInSeconds())
                        .recordDate(record.getCreatedAt())
                        .user(SimpleUserDto.from(record.getUser()))
                        .build())
                .toList();

        log.info("[랭킹 조회] 상위 {}명 조회 완료", ranking.size());

        // 2. 현재 유저의 랭킹 정보 조회
        FullRankInfo myRank = runningRecordRepository.findFullUserRankingInfo(
                userId,
                runningType.name(),
                gender,
                uniName
        ).orElse(null);

        if (myRank != null) {
            log.info("[내 랭킹 조회] userId={} → 순위: {}, 기록: {}초", userId, myRank.getRanking(), myRank.getRecordTimeInSeconds());
        } else {
            log.info("[내 랭킹 조회] userId={}는 해당 조건에서 기록이 없습니다.", userId);
        }

        OverallRunningRankDto myRecord = null;
        if (myRank != null) {
            myRecord = OverallRunningRankDto.builder()
                    .rank(myRank.getRanking())
                    .type(myRank.getRunningType())
                    .marathonName(myRank.getMarathonName())
                    .recordTimeInSeconds(myRank.getRecordTimeInSeconds())
                    .recordDate(myRank.getCreatedAt())
                    .totalCount(myRank.getTotalCount())
                    .user(SimpleUserDto.builder()
                            .id(myRank.getUserId())
                            .name(myRank.getUserName())
                            .email(null)
                            .gender(myRank.getUserGender())
                            .universityName(myRank.getUniversityName())
                            .studentNumber(null)
                            .profileImageUrl(null)
                            .majorName(null)
                            .build())
                    .build();
        }

        return new RunningRecordResponse(ranking, myRecord);
    }



}
