package com.runningRank.runningRank.user.domain;

import com.runningRank.runningRank.auth.dto.UserUpdateRequest;
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
    private Boolean isNameVisible = true;

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
    @Column(nullable = false)
    private Boolean isStudentNumberVisible = true;
    // 9
    @ManyToOne
    @JoinColumn(name = "major_id")
    private Major major;
    @Column(nullable = false)
    private Boolean isMajorVisible = true;

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

    /**
     * 사용자 정보를 업데이트하는 비즈니스 메서드
     * 이 메서드는 University와 Major 엔티티 객체를 직접 받도록 변경합니다.
     * DTO의 String 값을 엔티티로 변환하는 책임은 서비스 레이어에 있습니다.
     */
    public void updateInfo(UserUpdateRequest request, University newUniversity, Major newMajor) {

        if(request.getProfileImageUrl() != null){
            this.profileImageUrl = request.getProfileImageUrl();
        }
        if (request.getName() != null) {
            this.name = request.getName();
        }
        if (request.getBirthDate() != null) {
            this.birthDate = request.getBirthDate();
        }
        if (request.getGender() != null) {
            this.gender = Gender.valueOf(request.getGender());
        }
        if (request.getUniversityEmail() != null) {
            this.universityEmail = request.getUniversityEmail();
        }

        // University 엔티티가 변경되었을 경우에만 업데이트
        // (null이 아니고, 기존과 다른 경우)
        if (newUniversity != null && !newUniversity.equals(this.university)) {
            this.university = newUniversity;
            // 대학교가 변경되면 인증 상태 초기화
            this.isUniversityVerified = false;
        }

        // Major 엔티티가 변경되었을 경우에만 업데이트
        if (newMajor != null && !newMajor.equals(this.major)) {
            this.major = newMajor;
            // 전공이 변경되면 인증 상태 초기화
            // (보통 대학교 변경 시만 초기화하지만, 전공 변경도 포함할 수 있음)
            if (!request.isChangeUniversity()) { // 대학교 변경으로 이미 false가 된 경우가 아니라면
                this.isUniversityVerified = false;
            }
        }

        // isChangeUniversity는 명시적인 요청에 따라 isUniversityVerified를 초기화 (앞선 로직과 중복될 수 있으므로 조절)
        // 만약 isChangeUniversity가 '사용자가 대학교/전공 정보를 바꿨다고 명시적으로 체크한 경우'라면
        // 위에 University/Major 객체 변경으로 isUniversityVerified가 false 되는 로직보다 우선하거나 함께 적용되어야 합니다.
        // 여기서는 request.isChangeUniversity()가 true일 때 무조건 false로 만들도록 처리합니다.
        if (request.isChangeUniversity()) {
            this.isUniversityVerified = false;
        }
        // request.isChangeUniversity()가 false면 isUniversityVerified는 변경하지 않음
    }
}
