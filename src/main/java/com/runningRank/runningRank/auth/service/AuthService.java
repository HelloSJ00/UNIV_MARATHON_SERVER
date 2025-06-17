package com.runningRank.runningRank.auth.service;

import com.runningRank.runningRank.auth.dto.*;
import com.runningRank.runningRank.auth.jwt.JwtProvider;
import com.runningRank.runningRank.major.domain.Major;
import com.runningRank.runningRank.major.repository.MajorRepository;
import com.runningRank.runningRank.university.domain.University;
import com.runningRank.runningRank.university.repository.UniversityRepository;
import com.runningRank.runningRank.user.domain.Gender;
import com.runningRank.runningRank.user.domain.Role;
import com.runningRank.runningRank.user.repository.UserRepository;
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
                .profileImageUrl(request.getProfileImage())
                .role(Role.ROLE_USER)
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
                .build();
    }

    public TokenResponse login(LoginRequest request){
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("존재하지 않는 이메일입니다."));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("비밀번호가 일치하지 않습니다.");
        }
        // 인증 성공 시 JWT 발급
        String token = jwtProvider.createAccessToken(user.getEmail(), user.getRole());

        return new TokenResponse(token,"Bearer");
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
}
