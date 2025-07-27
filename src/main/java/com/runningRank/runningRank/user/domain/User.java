package com.runningRank.runningRank.user.domain;

import com.runningRank.runningRank.auth.dto.KakaoSignupRequest;
import com.runningRank.runningRank.auth.dto.SignUpRequest;
import com.runningRank.runningRank.auth.dto.UserUpdateRequest;
import com.runningRank.runningRank.major.domain.Major;
import com.runningRank.runningRank.runningRecord.domain.RunningRecord;
import com.runningRank.runningRank.university.domain.University;
import jakarta.persistence.*;
import jakarta.transaction.Transactional;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.security.crypto.password.PasswordEncoder;

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
@Slf4j
@Table(
        name = "user",
        indexes = {
                @Index(name = "idx_u_univId_gender_graduationStatus", columnList = "university_id, gender, graduationStatus")
        })
public class User {
    /**
     *  1. PK
     *  2. 계정
     *  3. 비밀번호
     *  + 3 - 2. 소셜 로그인용 outhProvider
     *  + 3 - 3. 소셜 로그인용 outhId
     *  4. 이름
     *  4-1 名 노출 여부
     *  5. 생년월일
     *  6. 성별
     *  7. 학교 (연관관계)
     *  8. 학번
     *  8-1 학번 노출 여부
     *  9. 전공 (연관관계)
     *  9-1 전공 노출 여부
     *  10. 프로필 이미지
     *  11. USER_ROLE
     *  12. 러닝 기록
     *  13. 학교 이메일
     *  14. 계정 생성일
     *  15. 학교 인증 여부
     *  16. 졸업 상태
     *  17-1 strava acessToken
     *  17-2 strava refreshToken
     *  17-3 expiredAt
     *  17-4 isStravaConnected
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

    // 4-1 이름 노출 여부
    @Column(nullable = false, columnDefinition = "BOOLEAN DEFAULT true")
    private Boolean isNameVisible = true;

    // 5
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
    // 8-1 학번 노출 여부
    @Column(nullable = false, columnDefinition = "BOOLEAN DEFAULT true")
    private Boolean isStudentNumberVisible = true;

    // 9
    @ManyToOne
    @JoinColumn(name = "major_id")
    private Major major;
    // 9 -1 학과 노출 여부
    @Column(nullable = false, columnDefinition = "BOOLEAN DEFAULT true")
    private Boolean isMajorVisible = true;

    // 10
    private String profileImageUrl;

    // 11
    @Enumerated(EnumType.STRING)
    private Role role;

    // 12. 러닝 기록 1:N = 유저 : 러닝기록
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RunningRecord> runningRecords = new ArrayList<>();

    // 13. 학생 이메일 검증을 위한 필드
    // 학교 이메일 인증 정보
    @Column(nullable = true, unique = true)
    private String universityEmail;

    // 14. 계정 생성일
    @Column(nullable = true)
    @CreatedDate
    private LocalDateTime createdAt;

    // 15. 학교 인증 여부
    @Column(nullable = false)
    private boolean isUniversityVerified;

    // 16. 졸업 상태
    @Enumerated(EnumType.STRING)
    private GraduationStatus graduationStatus;

    // 17-1 strava acessToken
    @Column(length = 255) // Access Token은 길이가 길 수 있음
    private String stravaAccessToken;

    // 17-2 strava refreshToken
    @Column(length = 255) // Refresh Token은 길이가 길 수 있음
    private String stravaRefreshToken;

    // 17-3 expiredAt
    private LocalDateTime stravaAccessTokenExpiresAt;

    // 17-4 isStravaAuthenticated. 학교 인증 여부
    @Column(nullable = false)
    private boolean isStravaConnected;

    // 외부 엔티티는 인자로 받아서 직접 주입
    public static User create(SignUpRequest request,
                              String encodedPassword, // 이미 암호화된 비밀번호를 받음
                              University university,
                              Major major) {
        return User.builder()
                .email(request.getEmail())
                .password(encodedPassword)
                .name(request.getName())
                .birthDate(request.getBirthDate())
                .gender(Gender.valueOf(request.getGender().toUpperCase()))
                .university(university)
                .major(major)
                .studentNumber(request.getStudentNumber())
                .profileImageUrl(request.getProfileImage())
                .role(Role.ROLE_USER) // User 생성 시 기본 역할 설정
                .isNameVisible(request.isNameVisible())
                .isStudentNumberVisible(request.isStudentNumberVisible())
                .isMajorVisible(request.isMajorVisible())
                .graduationStatus(GraduationStatus.valueOf(request.getGraduationStatus()))
                .build();
    }

    // 외부 엔티티는 인자로 받아서 직접 주입
    public static User kakaoCreate(KakaoSignupRequest request,
                                   String encodedPassword, // 이미 암호화된 비밀번호를 받음
                                   University university,
                                   Major major) {
        return User.builder()
                .email(request.getEmail())
                .password(encodedPassword)
                .name(request.getName())
                .birthDate(request.getBirthDate())
                .gender(Gender.valueOf(request.getGender().toUpperCase()))
                .university(university)
                .major(major)
                .studentNumber(request.getStudentNumber())
                .profileImageUrl(request.getProfileImage())
                .role(Role.ROLE_USER) // User 생성 시 기본 역할 설정
                .isNameVisible(request.isNameVisible())
                .isStudentNumberVisible(request.isStudentNumberVisible())
                .isMajorVisible(request.isMajorVisible())
                .graduationStatus(GraduationStatus.valueOf(request.getGraduationStatus()))
                .build();
    }

    public void verifyUnivEmail(String univEmail){
        this.universityEmail = univEmail;
        this.isUniversityVerified = true;
    }

    public int getAge() {
        return Period.between(this.birthDate, LocalDate.now()).getYears();
    }

    public void changePassword(String newPassword){
        this.password = newPassword;
    }
    /**
     * 사용자 정보를 업데이트하는 비즈니스 메서드
     * 이 메서드는 University와 Major 엔티티 객체를 직접 받도록 변경합니다.
     * DTO의 String 값을 엔티티로 변환하는 책임은 서비스 레이어에 있습니다.
     */
    public void updateInfo(UserUpdateRequest request, University newUniversity, Major newMajor) {
        log.info("Starting updateInfo for user ID: {}", this.id);
        log.debug("Request details: {}", request); // 디버그 레벨로 요청 DTO 내용 로그

        if (request.getProfileImageUrl() != null) {
            this.profileImageUrl = request.getProfileImageUrl();
            log.debug("Updated profileImageUrl to: {}", this.profileImageUrl);
        }
        if (request.getName() != null) {
            this.name = request.getName();
            log.debug("Updated name to: {}", this.name);
        }

        if (request.getGender() != null) {
            try {
                this.gender = Gender.valueOf(request.getGender().toUpperCase()); // 대소문자 문제 방지
                log.debug("Updated gender to: {}", this.gender);
            } catch (IllegalArgumentException e) {
                log.error("Invalid Gender value received: {}. Error: {}", request.getGender(), e.getMessage());
                // 에러 처리: 예외를 다시 던지거나, 기본값 설정 등
                throw e; // 예외를 다시 던져서 서비스 레이어에서 처리하도록 유도
            }
        }
        if (request.getUniversityEmail() != null) {
            this.universityEmail = request.getUniversityEmail();
            log.debug("Updated universityEmail to: {}", this.universityEmail);
        }

        // University 엔티티가 변경되었을 경우에만 업데이트
        if (newUniversity != null && !newUniversity.equals(this.university)) {
            log.debug("University changed from {} to {}", this.university.getUniversityName(), newUniversity.getUniversityName());
            this.university = newUniversity;
            this.isUniversityVerified = false;
            log.debug("isUniversityVerified set to false due to university change.");
        }

        // Major 엔티티가 변경되었을 경우에만 업데이트
        if (newMajor != null && !newMajor.equals(this.major)) {
            log.debug("Major changed from {} to {}", this.major.getName(), newMajor.getName());
            this.major = newMajor;
            if (!request.isChangeUniversity()) {
                this.isUniversityVerified = false;
                log.debug("isUniversityVerified set to false due to major change (not university change).");
            }
        }

        if (request.isChangeUniversity()) {
            this.isUniversityVerified = false;
            log.debug("isUniversityVerified explicitly set to false by request.isChangeUniversity.");
        }

        // 개인정보 노출 정보
        this.isNameVisible = request.isNameVisible();
        this.isStudentNumberVisible = request.isStudentNumberVisible();
        this.isMajorVisible = request.isMajorVisible();
        log.debug("Visibility settings: Name={}, StudentNumber={}, Major={}",
                this.isNameVisible, this.isStudentNumberVisible, this.isMajorVisible);

        // GraduationStatus 처리 개선
        if (request.getGraduationStatus() != null && !request.getGraduationStatus().trim().isEmpty()) { // .trim() 추가하여 공백 문자열도 처리
            try {
                this.graduationStatus = GraduationStatus.valueOf(request.getGraduationStatus().toUpperCase());
                log.debug("Updated graduationStatus to: {}", this.graduationStatus);
            } catch (IllegalArgumentException e) {
                log.error("Invalid GraduationStatus value received: {}. Error: {}", request.getGraduationStatus(), e.getMessage());
                // 유효하지 않은 값이라면, 어떻게 처리할지 결정해야 합니다.
                // 1. 예외를 다시 던져서 클라이언트에게 알림
                throw new IllegalArgumentException("유효하지 않은 졸업 상태 값입니다: " + request.getGraduationStatus());
                // 2. 현재 graduationStatus 값을 유지 (변경하지 않음)
                // 3. 특정 기본값으로 설정 (예: GraduationStatus.ENROLLED)
            }
        } else {
            // 요청에 graduationStatus가 없거나 빈 문자열/공백만 있을 경우
            log.debug("graduationStatus not provided or empty in request. Keeping current status: {}", this.graduationStatus);
            // 필요하다면 this.graduationStatus = null; 또는 특정 기본값으로 설정 가능
        }

        log.info("Finished updateInfo for user ID: {}", this.id);
    }

    public void updateStravaTokens(String newAccessToken, String newRefreshToken, LocalDateTime newExpiresAt) {
        this.isStravaConnected = true;
        this.stravaAccessToken = newAccessToken;
        this.stravaRefreshToken = newRefreshToken;
        this.stravaAccessTokenExpiresAt = newExpiresAt;
    }
}
