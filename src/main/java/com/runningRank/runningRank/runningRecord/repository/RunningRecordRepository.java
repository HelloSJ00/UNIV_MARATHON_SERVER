package com.runningRank.runningRank.runningRecord.repository;

import com.runningRank.runningRank.runningRecord.domain.RunningRecord;
import com.runningRank.runningRank.runningRecord.domain.RunningType;
import com.runningRank.runningRank.runningRecord.dto.FullRankInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface RunningRecordRepository extends JpaRepository<RunningRecord,Long> {

    /**
     * 학교별 종목별 러닝 기록 랭킹 조회
     * @param universityName
     * @param runningType
     * @return
     */
    @Query(value =/* language=SQL */
                """
                SELECT rr.* 
                FROM running_record rr 
                JOIN user u ON rr.user_id = u.id 
                JOIN university uni ON u.university_id = uni.id 
                WHERE rr.running_type = :runningType 
                  AND (:universityName IS NULL OR uni.university_name = :universityName)
                  AND (:gender = 'ALL' OR u.gender = :gender)
                ORDER BY rr.record_time_in_seconds ASC 
                LIMIT 100
                """, nativeQuery = true)
    List<RunningRecord> getTop100Rankings(
            @Param("runningType") String runningType,
            @Param("universityName") String universityName, // null이면 전체
            @Param("gender") String gender                  // ALL, MALE, FEMALE
    );

    /**
     * 유저가 몇등인지 반환하는 로직
     * @return
     */
    @Query(value = /* language=SQL */
    """
    SELECT ranked_records.*
    FROM (
        SELECT
            ranked.user_id AS userId,
            ranked.user_name AS userName,
            ranked.user_gender AS userGender,
            ranked.university_name AS universityName,
            ranked.running_type AS runningType,
            ranked.marathon_name AS marathonName,
            ranked.record_time_in_seconds AS recordTimeInSeconds,
            ranked.created_at AS createdAt,
            RANK() OVER (ORDER BY ranked.record_time_in_seconds ASC, ranked.user_id ASC) AS ranking,
            COUNT(*) OVER () AS totalCount
        FROM (
            SELECT 
                rr.running_type,
                rr.marathon_name,
                rr.record_time_in_seconds,
                rr.created_at,
                u.id AS user_id,
                u.name AS user_name,
                u.gender AS user_gender,
                uni.university_name
            FROM running_record rr
            JOIN user u ON rr.user_id = u.id
            JOIN university uni ON u.university_id = uni.id
            WHERE rr.running_type = :runningType
              AND (:gender = 'ALL' OR u.gender = :gender)
              AND (:universityName IS NULL OR uni.university_name = :universityName)
        ) ranked
    ) AS ranked_records
    WHERE userId = :userId
    """, nativeQuery = true)
    Optional<FullRankInfo> findFullUserRankingInfo(
            @Param("userId") Long userId,
            @Param("runningType") String runningType,
            @Param("gender") String gender,
            @Param("universityName") String universityName
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

    void deleteByUserIdAndRunningType(Long userId, RunningType runningType);
    Optional<RunningRecord> findByUserIdAndRunningType(Long userId,RunningType runningType);
}
