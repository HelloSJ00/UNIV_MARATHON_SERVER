package com.runningRank.runningRank.auth.dto;

import lombok.Getter;
import lombok.Setter; // 요청 본문을 받기 위해 @Setter도 추가하는 것이 일반적입니다.
import com.fasterxml.jackson.annotation.JsonProperty; // @JsonProperty 사용을 위해 임포트

import java.time.LocalDate;

@Getter
@Setter // DTO가 요청 본문을 매핑할 때 Setter가 필요합니다.
public class UserUpdateRequest {
    private String profileImageUrl;
    private String name;
    private String gender;
    private String universityEmail;
    private String universityName;
    private String studentNumber;
    // 프론트엔드에서 "majorName"으로 보내고 있으므로, 필드 이름을 "majorName"으로 변경
    private String majorName;
    private boolean isChangeUniversity;

    // is로 시작하는 boolean 필드에 @JsonProperty를 추가하여 명확하게 매핑합니다.
    @JsonProperty("isNameVisible")
    private boolean isNameVisible;
    @JsonProperty("isStudentNumberVisible")
    private boolean isStudentNumberVisible;
    @JsonProperty("isMajorVisible")
    private boolean isMajorVisible;

    private String graduationStatus;
}