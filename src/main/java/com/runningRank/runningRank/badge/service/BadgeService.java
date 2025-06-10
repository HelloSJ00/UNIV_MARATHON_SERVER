package com.runningRank.runningRank.badge.service;

import com.runningRank.runningRank.badge.domain.Badge;
import com.runningRank.runningRank.badge.domain.Rank;
import com.runningRank.runningRank.badge.repository.BadgeRepository;
import com.runningRank.runningRank.runningRecord.domain.RunningType;
import com.runningRank.runningRank.runningRecord.dto.RunningRankDto;
import com.runningRank.runningRank.runningRecord.repository.RunningRecordRepository;
import com.runningRank.runningRank.user.domain.School;
import com.runningRank.runningRank.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.time.LocalDate;
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
    public void top3bySchoolAndByRunningType(){
        // 1. User 테이블과 RunRecord 테이블 이너 조인 후
        // 2. RunningType,School 로 GroupBy 해서 상위 3명씩만
        List<Object[]> rows = runningRecordRepository.findTop3PerSchoolAndTypeAll();

        // Dto 로 변환
        List<RunningRankDto> dtos = rows.stream()
                .map(row -> new RunningRankDto(
                        (String) row[0],                         // school
                        (String) row[1],                         // type
                        ((BigInteger) row[2]).longValue(),       // userId
                        (Integer) row[3],                        // record_time_in_seconds
                        ((BigInteger) row[4]).intValue()         // rank
                ))
                .toList();

        // 3. 기존 배지 전체 삭제
        badgeRepository.deleteAll();

        // 4. 새로운 배지 등록
        List<Badge> badges = dtos.stream()
                .map(dto -> {
                    Rank rank = switch (dto.rank()) {
                        case 1 -> Rank.GOLD;
                        case 2 -> Rank.SILVER;
                        case 3 -> Rank.BRONZE;
                        default -> throw new IllegalStateException("Invalid rank: " + dto.rank());
                    };

                    return Badge.builder()
                            .school(School.valueOf(dto.school()))
                            .type(RunningType.valueOf(dto.type()))
                            .rank(rank)
                            .awardedAt(LocalDate.now())
                            .user(userRepository.getReferenceById(dto.userId()))
                            .build();
                })
                .toList();
        badgeRepository.saveAll(badges);
    }
}
