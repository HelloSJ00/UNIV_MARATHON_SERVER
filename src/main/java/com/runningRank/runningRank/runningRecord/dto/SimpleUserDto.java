package com.runningRank.runningRank.runningRecord.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@AllArgsConstructor
@Getter
public class SimpleUserDto {
    private Long id;
    private String name;
    private String email;
    private String universityName;
    private String studentNumber;
    private String profileImageUrl;
}
