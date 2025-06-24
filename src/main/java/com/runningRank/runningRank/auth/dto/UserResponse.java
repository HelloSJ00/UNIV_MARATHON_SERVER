package com.runningRank.runningRank.auth.dto;

import com.runningRank.runningRank.university.domain.University;
import com.runningRank.runningRank.user.domain.Gender;
import com.runningRank.runningRank.user.domain.Role;
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
}
