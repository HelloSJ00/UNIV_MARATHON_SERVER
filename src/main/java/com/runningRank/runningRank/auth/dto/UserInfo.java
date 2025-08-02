package com.runningRank.runningRank.auth.dto;

import com.runningRank.runningRank.mileage.domain.Mileage;
import com.runningRank.runningRank.runningRecord.domain.RunningRecord;
import com.runningRank.runningRank.user.domain.Role;
import com.runningRank.runningRank.user.domain.User;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
@Builder
public class UserInfo {
    private String email;
    private String name;
    private LocalDate birthDate;
    private String studentNumber;
    private int age;
    private String gender;
    private String universityName;
    private String majorName;
    private String profileImageUrl;
    private Role role;
    private LocalDateTime createdAt;
    private String universityEmail;
    private boolean isUniversityVerified;
    private boolean isNameVisible;
    private boolean isStudentNumberVisible;
    private boolean isMajorVisible;
    private String graduationStatus;
    private boolean isStravaConnected;

    private double totalDistanceKm;
    private int totalActivityCount;
    private int avgPaceTime;

    private Map<String, RunningRecordDto> runningRecords;

    public static UserInfo from(User user, Mileage mileage) {
        Map<String, RunningRecordDto> runningMap = new HashMap<>();

        for (String type : List.of("TEN_KM", "HALF", "FULL")) {
            user.getRunningRecords().stream()
                    .filter(r -> r.getRunningType().name().equals(type))
                    .findFirst()
                    .ifPresentOrElse(
                            r -> runningMap.put(type, RunningRecordDto.from(r)),
                            () -> runningMap.put(type, null)
                    );
        }

        // mileage가 null일 경우를 대비한 변수 초기화
        double totalDistanceKm = 0.0;
        int totalActivityCount = 0;
        int avgPaceTime = 0;

        // mileage 객체가 null이 아닐 때만 값을 가져옴
        if (mileage != null) {
            totalDistanceKm = mileage.getTotalDistanceKm();
            totalActivityCount = mileage.getTotalActivityCount();
            avgPaceTime = mileage.getAvgPaceTime();
        }

        return UserInfo.builder()
                .email(user.getEmail())
                .name(user.getName())
                .birthDate(user.getBirthDate())
                .age(user.getAge())
                .studentNumber(user.getStudentNumber())
                .gender(String.valueOf(user.getGender()))
                .universityName(user.getUniversity().getUniversityName())
                .majorName(user.getMajor().getName())
                .profileImageUrl(user.getProfileImageUrl())
                .role(user.getRole())
                .createdAt(user.getCreatedAt())
                .universityEmail(user.getUniversityEmail())
                .isUniversityVerified(user.isUniversityVerified())
                .isNameVisible(user.getIsNameVisible())
                .isStudentNumberVisible(user.getIsStudentNumberVisible())
                .isMajorVisible(user.getIsMajorVisible())
                .graduationStatus(String.valueOf(user.getGraduationStatus()))
                .runningRecords(runningMap)
                .isStravaConnected(user.isStravaConnected())
                .totalDistanceKm(totalDistanceKm) // null 체크 후 할당된 값 사용
                .totalActivityCount(totalActivityCount) // null 체크 후 할당된 값 사용
                .avgPaceTime(avgPaceTime) // null 체크 후 할당된 값 사용
                .build();
    }
}
