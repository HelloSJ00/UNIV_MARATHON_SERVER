package com.runningRank.runningRank.runningRecord.dto;

import com.runningRank.runningRank.user.domain.School;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@AllArgsConstructor
@Getter
public class SimpleUserDto {
    private Long id;
    private String name;
    private String email;
    private School school;
    private String studentNumber;
    private String profileImageUrl;
}
