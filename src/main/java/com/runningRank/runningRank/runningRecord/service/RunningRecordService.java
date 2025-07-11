package com.runningRank.runningRank.runningRecord.service;

import com.runningRank.runningRank.runningRecord.domain.RunningType;
import com.runningRank.runningRank.runningRecord.dto.MyRankInfo;
import com.runningRank.runningRank.runningRecord.dto.RunningRankDto;
import com.runningRank.runningRank.runningRecord.dto.RunningRecordResponse;
import com.runningRank.runningRank.runningRecord.repository.RunningRecordRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import java.util.Collections;
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
     * @param graduationStatus 졸업 여부 (예: STUDENT, GRADUATED, ALL)
     * @return 랭킹 리스트와 현재 유저의 개인 기록 포함된 응답 객체
     */
    public RunningRecordResponse getTop100RankingsWithMyRecord(
            Long userId,
            String universityName,
            RunningType runningType,
            String gender,
            String graduationStatus
    ) {
        String uniName = (universityName != null && !universityName.isBlank()) ? universityName : null;

        log.info("[랭킹 조회 시작] 종목: {}, 성별: {}, 학교: {}, 졸업여부: {}",
                runningType, gender, uniName != null ? uniName : "전체", graduationStatus);

        List<RunningRankDto> top100Rankings;
        MyRankInfo myRankInfo = null;

        try {
            // 1. 상위 100명 랭킹 조회 (캐싱된 헬퍼 메소드 호출)
            top100Rankings = getTop100RankingsInternal(uniName, runningType, gender, graduationStatus);
            log.debug("[상위 100명 랭킹 조회 성공] 데이터 로드 완료. 개수: {}", top100Rankings.size());

            // 랭킹 순위 부여 (캐시된 데이터에도 순위를 다시 부여해야 할 수 있습니다.
            // 만약 순위가 DB에서 이미 부여되어 온다면 이 부분은 불필요할 수 있습니다.)
            AtomicInteger rankCounter = new AtomicInteger(1);
            top100Rankings.forEach(record -> record.setRank(rankCounter.getAndIncrement()));


            // 2. 현재 유저의 랭킹 정보 조회 (캐싱하지 않음)
            myRankInfo = runningRecordRepository.findMyRankInfo(
                    userId,
                    runningType.name(),
                    gender,
                    uniName,
                    graduationStatus
            ).orElse(null);

            if (myRankInfo != null) {
                log.debug("[내 랭킹 조회 성공] userId={} → 순위: {}, 기록: {}초",
                        userId, myRankInfo.getRanking(), myRankInfo.getRecordTimeInSeconds());
            } else {
                log.debug("[내 랭킹 조회 완료] userId={}는 해당 조건에서 기록이 없습니다.", userId);
            }

        } catch (DataAccessException e) {
            log.error("[랭킹 데이터 조회 중 DB 오류 발생] 종목: {}, 성별: {}, 학교: {}. 오류: {}",
                    runningType, gender, uniName != null ? uniName : "전체", e.getMessage(), e);
            throw new RuntimeException("랭킹 정보를 불러오는 데 실패했습니다.", e);
        } catch (Exception e) {
            log.error("[랭킹 데이터 조회 중 예상치 못한 오류 발생] 종목: {}, 성별: {}, 학교: {}. 오류: {}",
                    runningType, gender, uniName != null ? uniName : "전체", e.getMessage(), e);
            throw new RuntimeException("알 수 없는 오류로 랭킹 정보를 불러올 수 없습니다.", e);
        }

        log.info("[랭킹 조회 완료] 최종 응답 객체 생성. 상위 {}명, 내 기록 {}존재",
                top100Rankings.size(), myRankInfo != null ? "정상" : "없음");

        return new RunningRecordResponse(top100Rankings, myRankInfo);
    }

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
