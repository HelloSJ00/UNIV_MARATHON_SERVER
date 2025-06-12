package com.runningRank.runningRank.auth.dto;

import com.runningRank.runningRank.university.domain.University;
import com.runningRank.runningRank.user.domain.Gender;
import com.runningRank.runningRank.user.domain.Role;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class UserResponse {
    private Long id;
    private String email;
    private String name;
    private int age;
    private Gender gender;
    private University university;
    private String studentNumber;
    private String major;
    private String profileImageUrl;
    private Role role;
}
