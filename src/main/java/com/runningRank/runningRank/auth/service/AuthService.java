package com.runningRank.runningRank.auth.service;

import com.runningRank.runningRank.auth.dto.LoginRequest;
import com.runningRank.runningRank.auth.dto.SignUpRequest;
import com.runningRank.runningRank.auth.dto.TokenResponse;
import com.runningRank.runningRank.auth.dto.UserResponse;
import com.runningRank.runningRank.auth.jwt.JwtProvider;
import com.runningRank.runningRank.user.domain.Gender;
import com.runningRank.runningRank.user.domain.School;
import com.runningRank.runningRank.user.domain.Role;
import com.runningRank.runningRank.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import com.runningRank.runningRank.user.domain.User;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;

    public UserResponse signup(SignUpRequest request) {
        // 이메일 중복 체크
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("이미 존재하는 이메일입니다.");
        }

        // User 객체 생성
        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword())) // 비밀번호 암호화
                .name(request.getName())
                .age(request.getAge())
                .gender(Gender.valueOf(request.getGender().toUpperCase())) // 변환
                .school(School.valueOf(request.getSchool().toUpperCase())) // 동일하게 처리 가능
                .major(request.getMajor())
                .profileImageUrl(request.getProfileImage())
                .role(Role.ROLE_USER)
                .build();

        // 저장
        User savedUser = userRepository.save(user);
        return UserResponse.builder()
                .id(savedUser.getId())
                .email(savedUser.getEmail())
                .name(savedUser.getName())
                .age(savedUser.getAge())
                .gender(savedUser.getGender())
                .school(savedUser.getSchool())
                .studentNumber(savedUser.getStudentNumber())
                .major(savedUser.getMajor())
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
}
