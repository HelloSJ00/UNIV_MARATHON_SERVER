package com.runningRank.runningRank.auth.dto;

import jakarta.validation.constraints.Email;
import lombok.Getter;

import java.time.LocalDate;

@Getter
public class SignUpRequest {

    // 1  이메일 (아이디)
    @Email
    private String email;

    // 2 비밀번호
    private String password;

    // 3. 이름
    private String name;          // 이름

    // 4. 나이 생년월일
    private LocalDate birthDate;           // 나이

    // 5. 성별
    private String gender;        // 성별 (예: "MALE", "FEMALE")

    // 6. 대학교
    private String university;    // 대학교 이름

    // 7. 학번
    private String studentNumber;     // 학번

    // 8. 전공
     private String major;         // 전공

    // 9.프로필 이미지
    private String profileImage;  // 프로필 이미지 URL (선택)

    private boolean isNameVisible;
    private boolean isStudentNumberVisible;
    private boolean isMajorVisible;
    private String graduationStatus;
}
