package com.runningRank.runningRank.universityRanking.repository;

import com.runningRank.runningRank.runningRecord.domain.RunningType;
import com.runningRank.runningRank.universityRanking.dto.FinisherUnivRanking;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class UniversityRankingRepository {

    private final JdbcTemplate jdbcTemplate;


    /**
     * 완주자 명수로 학교별 랭킹 조회
     * @return
     */
    public List<FinisherUnivRanking> getFinisherUniversityRankings(String runningType) {
        // SQL 쿼리 작성 (예시: 완주자 수 또는 완주율 기반 랭킹)
        // 실제 스키마와 랭킹 기준에 맞게 쿼리 수정이 필요합니다.
        // 여기서는 ROW_NUMBER()를 사용하여 랭킹을 직접 계산합니다.

        String sql = """
            SELECT
                RANK() OVER (ORDER BY COUNT(*) DESC) AS ranking,
                uni.university_name,
                uni.university_image_url AS university_image, -- DB에 이미지 URL 컬럼이 있다고 가정,
                COUNT(*) AS finisher_count
            FROM
                running_record rr
            LEFT JOIN
                user u ON rr.user_id = u.id
            LEFT JOIN
                university uni ON u.university_id = uni.id
            WHERE rr.running_type = ?
            GROUP BY
                uni.id,uni.university_name, uni.university_image_url 
            HAVING COUNT(*) > 0
            ORDER BY
                ranking ASC, uni.university_name ASC -- 랭킹과 이름 순으로 정렬
            LIMIT 30
            """; // 상위 30개 대학교만 가져오는 예시

        // JdbcTemplate.query() 메서드를 사용하여 쿼리를 실행하고 결과를 FinisherUnivRanking DTO에 매핑합니다.
        // RowMapper 람다를 사용하여 ResultSet의 각 행을 DTO 객체로 변환합니다.
        // --- 핵심 수정 부분 ---
        // jdbcTemplate.query()의 마지막 인자로 runningType 변수를 추가해야 합니다.
        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            return new FinisherUnivRanking(
                    rs.getInt("ranking"),
                    rs.getString("university_name"),
                    rs.getString("university_image"),
                    rs.getInt("finisher_count")
            );
        },runningType); // <--- 여기가 중요! 'runningType' 파라미터 추가
        // ---------------------
}}
