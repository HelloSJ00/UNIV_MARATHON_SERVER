package com.runningRank.runningRank.runningRecord.repository;

import com.runningRank.runningRank.runningRecord.domain.RunningRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface RunningRepository extends JpaRepository<RunningRecord,Long> {

    /**
     * 학교별 종목별 러닝 기록 랭킹 조회
     * @param school
     * @param type
     * @return
     */
    @Query(
            value = "SELECT rr.* " +
                    "FROM running_record rr " +
                    "JOIN user u ON rr.user_id = u.id " +
                    "WHERE u.school = :school " +
                    "AND rr.type = :type " +
                    "ORDER BY rr.record_time_in_seconds ASC "+
                    "LIMIT 100",
            nativeQuery = true
    )
    List<RunningRecord> findRankingBySchoolAndType(
            @Param("school") String school,   // Enum을 그대로 넘기면 toString()된 값으로 처리됨
            @Param("type") String type        // RaceType
    );

    /**
     * 종목별 통합 랭킹 순위
     * @param type
     * @return
     */
    @Query(
            value = "SELECT rr.* " +
                    "FROM running_record rr " +
                    "JOIN user u ON rr.user_id = u.id " +
                    "WHERE rr.type = :type " +
                    "ORDER BY rr.record_time_in_seconds ASC " +
                    "LIMIT 100",
            nativeQuery = true
    )
    List<RunningRecord> findTop100ByTypeOrderByRecordTimeAsc(
            @Param("type") String type
    );
}
