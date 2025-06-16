package com.runningRank.runningRank.user.domain;

import com.runningRank.runningRank.major.domain.Major;
import com.runningRank.runningRank.runningRecord.domain.RunningRecord;
import com.runningRank.runningRank.university.domain.University;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class User {

    /**
     *  1. PK
     *  2. 계정
     *  3. 비밀번호
     *  + 3 - 2. 소셜 로그인용 outhProvider
     *  + 3 - 3. 소셜 로그인용 outhId
     *  4. 이름
     *  5. 나이
     *  6. 성별
     *  7. 학교
     *  8. 학번
     *  9. 전공
     *      따로 테이블로 뻄
     *  10. 프로필 이미지
     *  11. USER_ROLE
     */

    /**
     * 추후 추가할 것들
     * 1. 뱃지
     */

    // 1
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 2
    @Column(nullable = false, unique = true)
    private String email;

    // 3
    private String password;

    // 3 - 2
    private String oauthProvider;

    // 3 - 3
    private String oauthId;

    // 4
    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private LocalDate birthDate;  // ex) 2000-05-14

    // 6
    // MALE,FEMALE
    @Enumerated(EnumType.STRING)
    private Gender gender;

    // 7
    // ex) SCHOOL_001
    // 6/11 수정 -> School 더이상 ENUM으로 저장하지 않고 따로 테이블 참조하게끔
    @ManyToOne
    @JoinColumn(name = "university_id")
    private University university;

    // 8
    private String studentNumber;

    // 9
    @ManyToOne
    @JoinColumn(name = "major_id")
    private Major major;

    // 10
    private String profileImageUrl;

    // 11
    @Enumerated(EnumType.STRING)
    private Role role;

    // 12. 러닝 기록 1:N = 유저 : 러닝기록
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RunningRecord> runningRecords = new ArrayList<>();

    // 13. 랭킹 뱃지 1:N = 유저 : 랭킹 뱃지

    // 14. 학생 이메일 검증을 위한 필드

    // 학교 이메일 인증 정보
    @Column(nullable = true, unique = true)
    private String universityEmail;

    // 16. 계정 생성일
    @Column(nullable = true)
    @CreatedDate
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private boolean isUniversityVerified;

    public void verifyUnivEmail(String univEmail){
        this.universityEmail = univEmail;
        this.isUniversityVerified = true;
    }

    public int getAge() {
        return Period.between(this.birthDate, LocalDate.now()).getYears();
    }
}
