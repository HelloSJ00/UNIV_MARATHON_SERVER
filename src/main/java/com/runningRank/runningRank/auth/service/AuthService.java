package com.runningRank.runningRank.auth.service;

import com.nimbusds.oauth2.sdk.TokenResponse;
import com.runningRank.runningRank.auth.dto.*;
import com.runningRank.runningRank.auth.jwt.JwtProvider;
import com.runningRank.runningRank.major.domain.Major;
import com.runningRank.runningRank.major.repository.MajorRepository;
import com.runningRank.runningRank.university.domain.University;
import com.runningRank.runningRank.university.repository.UniversityRepository;
import com.runningRank.runningRank.user.domain.Gender;
import com.runningRank.runningRank.user.domain.GraduationStatus;
import com.runningRank.runningRank.user.domain.Role;
import com.runningRank.runningRank.user.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import com.runningRank.runningRank.user.domain.User;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final MajorRepository majorRepository;
    private final PasswordEncoder passwordEncoder;
    private final UniversityRepository universityRepository;
    private final JwtProvider jwtProvider;

    // 이메일 중복확인
    public boolean checkEmailDuplicate(String email) {
        return userRepository.existsByEmail(email);
    }

    public UserResponse signup(SignUpRequest request) {
        // 이메일 중복 체크
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("이미 존재하는 이메일입니다.");
        }
        // 전공명으로만 조회시 중복 발생
        // 전공명,학교명 으로 Major 엔티티 조회
        Major major = majorRepository.findByNameAndUniversityName(request.getMajor(),request.getUniversity())
                .orElseThrow(() -> new IllegalArgumentException("해당 전공이 존재하지 않습니다."));

        University university = universityRepository.findByUniversityName(request.getUniversity())
                .orElseThrow(() -> new IllegalArgumentException("해당 학교가 존재하지 않습니다."));
        // User 객체 생성
        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword())) // 비밀번호 암호화
                .name(request.getName())
                .birthDate(request.getBirthDate())
                .gender(Gender.valueOf(request.getGender().toUpperCase())) // 변환
                .university(university) // 동일하게 처리 가능
                .major(major)  // 변경된 부분
                .studentNumber(request.getStudentId())
                .profileImageUrl(request.getProfileImage())
                .role(Role.ROLE_USER)
                .isNameVisible(request.isNameVisible())
                .isStudentNumberVisible(request.isStudentNumberVisible())
                .isMajorVisible(request.isMajorVisible())
                .graduationStatus(GraduationStatus.valueOf(request.getGraduationStatus()))
                .build();
        // 저장
        User savedUser = userRepository.save(user);
        return UserResponse.builder()
                .id(savedUser.getId())
                .email(savedUser.getEmail())
                .name(savedUser.getName())
                .birthDate(request.getBirthDate())
                .gender(savedUser.getGender())
                .university(savedUser.getUniversity())
                .studentNumber(savedUser.getStudentNumber())
                .major(savedUser.getMajor().getName())
                .profileImageUrl(savedUser.getProfileImageUrl())
                .role(savedUser.getRole())
                .isNameVisible(savedUser.getIsNameVisible())
                .isStudentNumberVisible(savedUser.getIsStudentNumberVisible())
                .isMajorVisible(savedUser.getIsMajorVisible())
                .graduationStatus(String.valueOf(savedUser.getGraduationStatus()))
                .build();
    }

    public LoginResponse login(LoginRequest request){
        // 1. 유저 조회
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("존재하지 않는 이메일입니다."));

        // 2. 비밀번호 확인
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("비밀번호가 일치하지 않습니다.");
        }

        // 3. 토큰 생성
        String token = jwtProvider.createAccessToken(user.getEmail(), user.getRole());

        // 4. 유저 정보 DTO 생성 (러닝기록 포함해서 정리)
        UserInfo userInfo = UserInfo.from(user);

        // 5. 통합 응답
        return LoginResponse.builder()
                .accessToken(token)
                .tokenType("Bearer")
                .user(userInfo)
                .build();
    }

    /**
     * 회원가입시 등록된 모든 학교 조회
     * @return
     */
    public List<String> getAllUniversityNames() {
        return universityRepository.findAll().stream()
                .map(University::getUniversityName) // universityName 필드만 추출
                .toList(); // Java 16 이상. Java 8~11이면 .collect(Collectors.toList())
    }

    /**
     * 학교 선택시 해당 학교 전공 조회
     */
    public List<String> getMajorsByUniversityName(String universityName) {
        List<Major> majors = majorRepository.findByUniversityName(universityName);
        return majors.stream()
                .map(Major::getName)
                .toList();
    }

    /**
     * 내 정보 수정
     */
    @Transactional
    public boolean updateUserInfo(UserUpdateRequest request, Long userId) {
        User user = userRepository.getReferenceById(userId); // 사용자를 찾거나 프록시 로드

        University newUniversity = null;
        Major newMajor = null;

        // 1. 대학교 이름으로 University 엔티티 조회 (요청에 universityName이 있다면)
        if (request.getUniversityName() != null && !request.getUniversityName().isEmpty()) {
            newUniversity = universityRepository.findByUniversityName(request.getUniversityName())
                    .orElseThrow(() -> new EntityNotFoundException("대학교를 찾을 수 없습니다: " + request.getUniversityName()));
        }

        // 2. 전공 이름과 (선택적으로) 대학교로 Major 엔티티 조회 (요청에 major가 있다면)
        // 전공은 특정 대학교에 속하는 경우가 많으므로, 대학교와 전공명을 함께 사용하여 조회하는 것이 일반적입니다.
        if (request.getMajor() != null && !request.getMajor().isEmpty()) {
                newMajor = majorRepository.findByNameAndUniversityName(request.getMajor(), newUniversity.getUniversityName())
                        .orElseThrow(() -> new EntityNotFoundException("해당 대학교에서 전공을 찾을 수 없습니다: " + request.getMajor()));
            }

        // 3. User 엔티티의 업데이트 메서드 호출
        // isChangeUniversity 플래그와 조회된 엔티티들을 함께 전달
        user.updateInfo(request, newUniversity, newMajor);

        // @Transactional 덕분에 변경사항이 자동으로 영속화됩니다.
        // userRepository.save(user); // 명시적 save는 필수는 아니지만, 명확성을 위해 사용 가능

        return true;
    }
}
