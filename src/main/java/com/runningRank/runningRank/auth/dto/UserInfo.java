package com.runningRank.runningRank.auth.dto;

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
    private int age;
    private String gender;
    private String universityName;
    private String majorName;
    private String profileImageUrl;
    private Role role;
    private LocalDateTime createdAt;
    private String universityEmail;
    private boolean isUniversityVerified;
//    private String graduationStatus;

    private Map<String, RunningRecordDto> runningRecords;

    public static UserInfo from(User user) {
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
        return UserInfo.builder()
                .email(user.getEmail())
                .name(user.getName())
                .birthDate(user.getBirthDate())
                .age(user.getAge())
                .gender(java.lang.String.valueOf(user.getGender()))
                .universityName(user.getUniversity().getUniversityName()) // University 엔티티에 getName()이 있다고 가정
                .majorName(user.getMajor().getName())           // Major 엔티티에 getName()이 있다고 가정
                .profileImageUrl(user.getProfileImageUrl())
                .role(user.getRole())
                .createdAt(user.getCreatedAt())
                .universityEmail(user.getUniversityEmail())
                .isUniversityVerified(user.isUniversityVerified())
                .runningRecords(runningMap)
                .build();
    }
}
