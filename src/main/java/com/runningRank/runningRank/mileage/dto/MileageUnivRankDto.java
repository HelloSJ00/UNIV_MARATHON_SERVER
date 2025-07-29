package com.runningRank.runningRank.mileage.dto;

import com.runningRank.runningRank.runningRecord.domain.RunningType;
import lombok.Getter;

@Getter
public class MileageUnivRankDto {
    // 쿼리에서 직접 가져오는 필드들
    private Long userId; // u.user_id
    private String name; // u.name
    private String gender; // u.gender
    private String universityName; // uni.university_name
    private String studentNumber; // u.studnetNumber (오타 주의: studentNumber)
    private String profileImageUrl; // u.profileImage
    private String majorName; // m.name
    private boolean isNameVisible;
    private boolean isStudentNumberVisible;
    private boolean isMajorVisible;
    private String graduationStatus;

    // 쿼리 결과에 없는 필드는 제거하거나, 아래와 같이 나중에 설정하는 방식으로 변경
    private int rank;

    // 마일리지 정보들
    private double totalDistanceKm;    // <-- 중요: Mileage 엔티티와 동일하게 'double'
    private int totalActivityCount;    // <-- 중요: Mileage 엔티티와 동일하게 'int'
    private int avgPaceTime;           // <-- 중요: Mileage 엔티티와 동일하게 'int'

    public MileageUnivRankDto(
            Long userId,
            String name,
            String gender,
            String universityName,
            String studentNumber,
            String profileImageUrl,
            String majorName,
            boolean isNameVisible,
            boolean isStudentNumberVisible,
            boolean isMajorVisible,
            String graduationStatus,
            double totalDistanceKm,
            int totalActivityCount,
            int avgPaceTime
    ) {
        this.userId = userId;
        this.name = name;
        this.gender = gender;
        this.universityName = universityName;
        this.studentNumber = studentNumber;
        this.profileImageUrl = profileImageUrl;
        this.majorName = majorName;
        this.isNameVisible = isNameVisible;
        this.isStudentNumberVisible = isStudentNumberVisible;
        this.isMajorVisible = isMajorVisible;
        this.graduationStatus = graduationStatus;
        this.rank = 0;
        this.totalDistanceKm = totalDistanceKm;
        this.totalActivityCount = totalActivityCount;
        this.avgPaceTime = avgPaceTime;
    };

    public void setRank(int rank) {
        this.rank = rank;
    }
}
