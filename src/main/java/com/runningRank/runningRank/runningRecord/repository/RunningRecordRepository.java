package com.runningRank.runningRank.runningRecord.repository;

import com.runningRank.runningRank.runningRecord.domain.RunningRecord;
import com.runningRank.runningRank.runningRecord.domain.RunningType;
import com.runningRank.runningRank.runningRecord.dto.MyRankInfo;
import com.runningRank.runningRank.runningRecord.dto.RunningRankDto;
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
            SELECT
                rr.running_type,
                rr.marathon_name,
                rr.record_time_in_seconds,
                u.id AS user_id, -- DTO의 userId에 매핑되도록 별칭 사용
                u.name AS user_name, -- DTO의 name에 매핑되도록 별칭 사용
                u.email AS user_email, -- DTO의 email에 매핑되도록 별칭 사용
                u.gender AS user_gender, -- DTO의 gender에 매핑되도록 별칭 사용
                uni.university_name,
                u.student_number, -- DB 컬럼명 확인 후 수정
                u.profile_image_url AS profile_image_url, -- DTO의 profileImageUrl에 매핑되도록 별칭 사용
                m.name AS major_name, -- DTO의 majorName에 매핑되도록 별칭 사용
                u.is_name_visible,
                u.is_student_number_visible,
                u.is_major_visible,
                u.graduation_status
            FROM running_record rr
            JOIN user u ON rr.user_id = u.id
            JOIN university uni ON u.university_id = uni.id
            JOIN major m ON u.major_id = m.id
            WHERE rr.running_type = :runningType
              AND (:universityName IS NULL OR uni.university_name = :universityName)
              AND (:gender = 'ALL' OR u.gender = :gender)
            ORDER BY rr.record_time_in_seconds ASC
            LIMIT 100
            """, nativeQuery = true)
    List<RunningRankDto> getTop100Rankings(
            @Param("runningType") String runningType, // 또는 RunningType runningType
            @Param("universityName") String universityName,
            @Param("gender") String gender
    );

    /**
     * 유저가 몇등인지 반환하는 로직
     * @return
     */
    @Query(value =
            """
            SELECT
                ranked_records.record_time_in_seconds AS recordTimeInSeconds,
                ranked_records.ranking,
                ranked_records.totalCount,
                ranked_records.gender,
                ranked_records.running_type,
                ranked_records.graduation_status
            FROM (
                SELECT
                    ranked.record_time_in_seconds,
                    ranked.user_id AS userId,
                    ranked.gender,
                    ranked.running_type,
                    ranked.graduation_status,
                    RANK() OVER (ORDER BY ranked.record_time_in_seconds ASC, ranked.user_id ASC) AS ranking, /* 'rank' 대신 'computed_rank' 사용 */
                    COUNT(*) OVER () AS totalCount
                FROM (
                    SELECT
                        rr.record_time_in_seconds,
                        u.id AS user_id,
                        u.gender,
                        rr.running_type,
                        u.graduation_status
                    FROM running_record rr
                    JOIN user u ON rr.user_id = u.id
                    JOIN university uni ON u.university_id = uni.id
                    WHERE rr.running_type = :runningType
                      AND (:gender = 'ALL' OR u.gender = :gender)
                      AND (:universityName IS NULL OR uni.university_name = :universityName)
                ) ranked
            ) AS ranked_records
            WHERE ranked_records.userId = :userId
            """, nativeQuery = true)
    Optional<MyRankInfo> findMyRankInfo(
            @Param("userId") Long userId,
            @Param("runningType") String runningType,
            @Param("gender") String gender,
            @Param("universityName") String universityName
    );

//    @Query(
//            value = "SELECT university_name, running_type, user_id, record_time_in_seconds, rnk " +
//                    "FROM ( " +
//                    "  SELECT r.*, uni.university_name, " +
//                    "         RANK() OVER ( " +
//                    "           PARTITION BY uni.university_name, r.running_type " +
//                    "           ORDER BY r.record_time_in_seconds ASC " +
//                    "         ) AS rnk " +
//                    "  FROM running_record r " +
//                    "  JOIN user u ON r.user_id = u.id " +
//                    "  JOIN university uni ON u.university_id = uni.id " +
//                    ") ranked " +
//                    "WHERE ranked.rnk <= 3",
//            nativeQuery = true
//    )
//    List<Object[]> findTop3PerSchoolAndTypeAll();
//
//    void deleteByUserIdAndRunningType(Long userId, RunningType runningType);
    Optional<RunningRecord> findByUserIdAndRunningType(Long userId,RunningType runningType);
}
