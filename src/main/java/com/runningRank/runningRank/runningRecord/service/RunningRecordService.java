package com.runningRank.runningRank.runningRecord.service;

import com.runningRank.runningRank.runningRecord.domain.RunningType;
import com.runningRank.runningRank.runningRecord.dto.MyRankInfo;
import com.runningRank.runningRank.runningRecord.dto.RunningRankDto;
import com.runningRank.runningRank.runningRecord.dto.RunningRecordResponse;
import com.runningRank.runningRank.runningRecord.repository.RunningRecordRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

        List<RunningRankDto> records = null; // 초기화
        try {
            records = runningRecordRepository.getTop100Rankings(
                    runningType.name(),
                    uniName,
                    gender
            );
            log.info("[쿼리 실행 완료] 랭킹 데이터 조회 성공."); // 이 로그가 찍히는지 확인
        } catch (Exception e) {
            log.error("[랭킹 조회 중 오류 발생]", e); // 오류 발생 시 스택 트레이스 출력
            throw e; // 예외를 다시 던져서 클라이언트에게도 전달
        }

        // records가 null일 경우를 대비한 방어 로직 추가 (필요시)
        if (records == null) {
            return new RunningRecordResponse(Collections.emptyList(), null);
        }

        AtomicInteger rankCounter = new AtomicInteger(1);
        records.forEach(record -> record.setRank(rankCounter.getAndIncrement()));
        List<RunningRankDto> ranking = records;

        log.info("[랭킹 조회] 상위 {}명 조회 완료", ranking.size());

        // 2. 현재 유저의 랭킹 정보 조회
        MyRankInfo myRank = null; // 초기화

        try {
            // 2. 현재 유저의 랭킹 정보 조회
            myRank = runningRecordRepository.findMyRankInfo(
                    userId,
                    runningType.name(),
                    gender,
                    uniName
            ).orElse(null);

            if (myRank != null) {
                log.info("[내 랭킹 조회 성공] userId={} → 순위: {}, 기록: {}초", userId, myRank.getRanking(), myRank.getRecordTimeInSeconds());
                // MyRankInfo의 getter가 getRank()인 것으로 가정합니다.
                // getRanking()이 아니라 getRank()가 올바른 getter일 겁니다.
            } else {
                log.info("[내 랭킹 조회 완료] userId={}는 해당 조건에서 기록이 없습니다.", userId);
            }
        } catch (DataAccessException e) {
            // Spring Data JPA 관련 예외 (SQL 에러, DB 연결 문제 등)를 잡습니다.
            log.error("[내 랭킹 조회 중 DB 오류 발생] userId={}: {}", userId, e.getMessage(), e);
            // 필요에 따라 사용자에게 오류 메시지를 반환하거나, 기본값을 설정할 수 있습니다.
            // 예: throw new ServiceException("랭킹 정보를 불러오는 데 실패했습니다.", e);
        } catch (Exception e) {
            // 그 외 예상치 못한 모든 예외를 잡습니다.
            log.error("[내 랭킹 조회 중 알 수 없는 오류 발생] userId={}: {}", userId, e.getMessage(), e);
            // 필요에 따라 사용자에게 오류 메시지를 반환하거나, 기본값을 설정할 수 있습니다.
        }


        return new RunningRecordResponse(ranking, myRank);
    }



}
