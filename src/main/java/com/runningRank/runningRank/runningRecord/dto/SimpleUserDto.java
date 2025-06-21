package com.runningRank.runningRank.runningRecord.dto;

import com.runningRank.runningRank.user.domain.User;
import lombok.*;

@AllArgsConstructor
@Getter
@Builder
@NoArgsConstructor
public class SimpleUserDto {
    private Long id;
    private String name;
    private String email;
    private String gender;
    private String universityName;
    private String studentNumber;
    private String profileImageUrl;
    private String majorName;

    public static SimpleUserDto from(User user) {
        return SimpleUserDto.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .gender(String.valueOf(user.getGender()))
                .universityName(user.getUniversity().getUniversityName())
                .studentNumber(user.getStudentNumber())
                .profileImageUrl(user.getProfileImageUrl())
                .majorName(user.getMajor().getName())
                .build();
    }

}
