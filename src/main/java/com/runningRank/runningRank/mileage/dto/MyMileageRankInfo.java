package com.runningRank.runningRank.mileage.dto;

import lombok.Getter; // Lombok 사용 시

@Getter // 모든 필드에 대한 getter 메서드 자동 생성
public class MyMileageRankInfo {

    private Double totalDistanceKm; // SQL의 total_distance_km (보통 거리 값은 double/float)
    private Integer totalActivityCount; // SQL의 total_activity_count (활동 횟수는 정수)
    private Integer avgPaceTime; // SQL의 avg_pace_time (페이스 시간도 소수점 있을 수 있음)
    private Long ranking; // SQL의 ranking (RANK() OVER 함수 결과는 보통 Long)
    private Long totalCount; // SQL의 totalCount (COUNT(*) OVER () 결과도 보통 Long)
    private String gender; // SQL의 gender
    private String graduationStatus; // SQL의 graduation_status

    // Lombok을 사용하지 않는 경우, 아래 생성자를 수동으로 추가해야 합니다.

    public MyMileageRankInfo(
            Double totalDistanceKm,
            Integer totalActivityCount,
            Integer avgPaceTime,
            Long ranking,
            Long totalCount,
            String gender,
            String graduationStatus
    ) {
        this.totalDistanceKm = totalDistanceKm;
        this.totalActivityCount = totalActivityCount;
        this.avgPaceTime = avgPaceTime;
        this.ranking = ranking;
        this.totalCount = totalCount;
        this.gender = gender;
        this.graduationStatus = graduationStatus;
    }
}