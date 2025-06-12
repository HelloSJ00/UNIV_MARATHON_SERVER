package com.runningRank.runningRank.badge.service;

import com.runningRank.runningRank.badge.domain.Badge;
import com.runningRank.runningRank.badge.domain.RunningRank;
import com.runningRank.runningRank.badge.repository.BadgeRepository;
import com.runningRank.runningRank.runningRecord.domain.RunningType;
import com.runningRank.runningRank.runningRecord.dto.RunningRankDto;
import com.runningRank.runningRank.runningRecord.repository.RunningRecordRepository;
import com.runningRank.runningRank.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class BadgeService {

    private final BadgeRepository badgeRepository;
    private final UserRepository userRepository;
    private final RunningRecordRepository runningRecordRepository;

    /**
     * 러닝 랭킹 매기는 로직
     */
    @Transactional
    @Scheduled(cron = "0 0 0 * * *") // 매일 자정 0시 0분 0초
    public void top3bySchoolAndByRunningType(){
        log.info("🎯 자정 배치 작업 실행됨: {}", LocalDateTime.now());
        // 1. User 테이블과 RunRecord 테이블 이너 조인 후
        // 2. RunningType,School 로 GroupBy 해서 상위 3명씩만
        List<Object[]> rows = runningRecordRepository.findTop3PerSchoolAndTypeAll();

        // Dto 로 변환
        List<RunningRankDto> dtos = rows.stream()
                .map(row -> new RunningRankDto(
                        (String) row[0],                         // school
                        (String) row[1],                         // type
                        ((Number) row[2]).longValue(),           // userId
                        (Integer) row[3],                        // record_time_in_seconds
                        ((Number) row[4]).intValue()             // rank
                ))
                .toList();

        // 3. 기존 배지 전체 삭제
        badgeRepository.deleteAll();

        /**
         * 4. 새로운 배지 등록
         * 1등은 GOLD 2등은 SILVER 3등은 BRONZE
         */
        List<Badge> badges = dtos.stream()
                .map(dto -> {
                    RunningRank runningRank = switch (dto.rank()) {
                        case 1 -> RunningRank.GOLD;
                        case 2 -> RunningRank.SILVER;
                        case 3 -> RunningRank.BRONZE;
                        default -> throw new IllegalStateException("Invalid rank: " + dto.rank());
                    };

                    return Badge.builder()
                            .unversity(dto.university())
                            .type(RunningType.valueOf(dto.type()))
                            .runningRank(runningRank)
                            .awardedAt(LocalDate.now())
                            .user(userRepository.getReferenceById(dto.userId()))
                            .build();
                })
                .toList();

        // 5. 저장
        badgeRepository.saveAll(badges);
    }
}
