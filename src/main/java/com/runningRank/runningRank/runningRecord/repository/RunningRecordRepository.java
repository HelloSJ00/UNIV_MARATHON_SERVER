package com.runningRank.runningRank.runningRecord.repository;

import com.runningRank.runningRank.runningRecord.domain.RunningRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface RunningRecordRepository extends JpaRepository<RunningRecord,Long> {

    /**
     * 학교별 종목별 러닝 기록 랭킹 조회
     * @param universityName
     * @param runningType
     * @return
     */
    @Query(
            value = "SELECT rr.* " +
                    "FROM running_record rr " +
                    "JOIN user u ON rr.user_id = u.id " +
                    "JOIN university uni ON u.university_id = uni.id " +
                    "WHERE uni.university_name = :universityName " +
                    "AND rr.running_type = :runningType " +
                    "ORDER BY rr.record_time_in_seconds ASC " +
                    "LIMIT 100",
            nativeQuery = true
    )
    List<RunningRecord> findRankingBySchoolAndType(
            @Param("universityName") String universityName,
            @Param("runningType") String runningType
    );

    /**
     * 종목별 통합 랭킹 순위
     * @param runningType
     * @return
     */
    @Query(
            value = "SELECT rr.* " +
                    "FROM running_record rr " +
                    "JOIN user u ON rr.user_id = u.id " +
                    "WHERE rr.running_type = :runningType " +
                    "ORDER BY rr.record_time_in_seconds ASC " +
                    "LIMIT 100",
            nativeQuery = true
    )
    List<RunningRecord> findTop100ByTypeOrderByRecordTimeAsc(
            @Param("runningType") String runningType
    );

    @Query(
            value = "SELECT university_name, running_type, user_id, record_time_in_seconds, rnk " +
                    "FROM ( " +
                    "  SELECT r.*, uni.university_name, " +
                    "         RANK() OVER ( " +
                    "           PARTITION BY uni.university_name, r.running_type " +
                    "           ORDER BY r.record_time_in_seconds ASC " +
                    "         ) AS rnk " +
                    "  FROM running_record r " +
                    "  JOIN user u ON r.user_id = u.id " +
                    "  JOIN university uni ON u.university_id = uni.id " +
                    ") ranked " +
                    "WHERE ranked.rnk <= 3",
            nativeQuery = true
    )
    List<Object[]> findTop3PerSchoolAndTypeAll();
}
