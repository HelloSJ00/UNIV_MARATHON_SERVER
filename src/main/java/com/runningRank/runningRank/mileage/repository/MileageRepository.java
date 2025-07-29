package com.runningRank.runningRank.mileage.repository;

import com.runningRank.runningRank.mileage.domain.Mileage;
import com.runningRank.runningRank.mileage.dto.MileageUnivRankDto;
import com.runningRank.runningRank.mileage.dto.MyMileageRankInfo;
import com.runningRank.runningRank.runningRecord.dto.MyRankInfo;
import com.runningRank.runningRank.runningRecord.dto.RunningRankDto;
import com.runningRank.runningRank.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface MileageRepository extends JpaRepository<Mileage, Long> {
    Optional<Mileage> findByUserAndYearAndMonth(User user, int year, int month);
    List<Mileage> findAllByYearAndMonth(int year, int month);

    @Query(value =/* language=SQL */
            """
            SELECT
                u.id AS userId,
                u.name AS name,
                u.gender AS gender,
                uni.university_name,
                u.student_number,
                u.profile_image_url AS profileImageUrl,
                maj.name AS majorName, -- major 테이블의 새로운 별칭 'maj' 사용
                u.is_name_visible AS isNameVisible,
                u.is_student_number_visible AS isStudentNumberVisible,
                u.is_major_visible AS isMajorVisible,
                u.graduation_status AS graduationStatus,
                m.total_distance_km AS totalDistanceKm, -- mileage 테이블의 m
                m.total_activity_count AS totalActivityCount, -- mileage 테이블의 m
                m.avg_pace_time AS avgPaceTime -- mileage 테이블의 m
            FROM mileage m
            JOIN user u ON m.user_id = u.id
            JOIN university uni ON u.university_id = uni.id
            JOIN major maj ON u.major_id = maj.id -- 여기를 'maj'로 변경!
            WHERE m.month = :month
              AND m.year = :year
              AND (:universityName IS NULL OR uni.university_name = :universityName)
              AND (:gender = 'ALL' OR u.gender = :gender)
              AND (:graduationStatus = 'ALL' OR u.graduation_status = :graduationStatus)
            ORDER BY m.total_distance_km DESC
            LIMIT 100
            """, nativeQuery = true)
    List<MileageUnivRankDto> getTop100MileageRankings(
            @Param("month") int month,
            @Param("year") int year,
            @Param("universityName") String universityName,
            @Param("gender") String gender,
            @Param("graduationStatus") String graduationStatus
    );

    /**
     * 유저가 몇등인지 반환하는 로직
     * @return
     */
    @Query(value =
            """
            SELECT
                ranked_records.total_distance_km AS totalDistanceKm,
                ranked_records.total_activity_count AS totalActivityCount,
                ranked_records.avg_pace_time AS avgPaceTime,
                ranked_records.ranking,
                ranked_records.totalCount,
                ranked_records.gender,
                ranked_records.graduation_status
            FROM (
                SELECT
                    ranked.total_distance_km,
                    ranked.total_activity_count,
                    ranked.avg_pace_time,
                    ranked.user_id AS userId,
                    ranked.gender,
                    ranked.graduation_status,
                    RANK() OVER (ORDER BY ranked.total_distance_km ASC, ranked.user_id ASC) AS ranking, /* 'rank' 대신 'computed_rank' 사용 */
                    COUNT(*) OVER () AS totalCount
                FROM (
                    SELECT
                        m.total_distance_km,
                        m.total_activity_count,
                        m.avg_pace_time,
                        u.id AS user_id,
                        u.gender,
                        u.graduation_status
                    FROM (SELECT * FROM mileage WHERE mileage.year = :year AND mileage.month = :month) m
                    JOIN user u ON m.user_id = u.id
                    JOIN university uni ON u.university_id = uni.id
                    WHERE (:gender = 'ALL' OR u.gender = :gender)
                      AND (:universityName IS NULL OR uni.university_name = :universityName)
                      AND (:graduationStatus = 'ALL' OR u.graduation_status = :graduationStatus)
                ) ranked
            ) AS ranked_records
            WHERE ranked_records.userId = :userId
            """, nativeQuery = true)
    Optional<MyMileageRankInfo> findMyMileageRankInfo(
            @Param("year") int year,
            @Param("month") int month,
            @Param("userId") Long userId,
            @Param("gender") String gender,
            @Param("universityName") String universityName,
            @Param("graduationStatus") String graduationStatus
    );
}
