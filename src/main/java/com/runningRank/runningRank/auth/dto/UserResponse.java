package com.runningRank.runningRank.auth.dto;

import com.runningRank.runningRank.user.domain.Gender;
import com.runningRank.runningRank.user.domain.Role;
import com.runningRank.runningRank.user.domain.School;
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
    private School school;
    private String studentNumber;
    private String major;
    private String profileImageUrl;
    private Role role;
}
