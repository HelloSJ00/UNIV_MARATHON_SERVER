package com.runningRank.runningRank.auth.dto;

import com.runningRank.runningRank.university.domain.University;
import com.runningRank.runningRank.user.domain.Gender;
import com.runningRank.runningRank.user.domain.Role;
import com.runningRank.runningRank.user.domain.User;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Builder
@Getter
public class UserResponse {
    private Long id;
    private String email;
    private String name;
    private LocalDate birthDate;
    private Gender gender;
    private University university;
    private String studentNumber;
    private String major;
    private String profileImageUrl;
    private Role role;
    private boolean isNameVisible;
    private boolean isStudentNumberVisible;
    private boolean isMajorVisible;
    private String graduationStatus;

    public static UserResponse from(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .name(user.getName())
                .birthDate(user.getBirthDate()) // User 엔티티에 birthDate getter 추가 권장
                .gender(user.getGender())
                .university(user.getUniversity()) // user.getUniversity().getName() 등으로 변경될 수 있음
                .studentNumber(user.getStudentNumber())
                .major(user.getMajor().getName()) // Major 엔티티에 name getter 추가 권장
                .profileImageUrl(user.getProfileImageUrl())
                .role(user.getRole())
                .isNameVisible(user.getIsNameVisible())
                .isStudentNumberVisible(user.getIsStudentNumberVisible())
                .isMajorVisible(user.getIsMajorVisible())
                .graduationStatus(String.valueOf(user.getGraduationStatus()))
                .build();
    }
}
