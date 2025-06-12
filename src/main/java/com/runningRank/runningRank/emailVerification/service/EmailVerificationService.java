package com.runningRank.runningRank.emailVerification.service;

import com.runningRank.runningRank.university.repository.UniversityRepository;
import com.runningRank.runningRank.user.domain.User;
import com.runningRank.runningRank.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailVerificationService {

    private final UserRepository userRepository;

    /**
     * 이메일 도메인이 유저 학교의 도메인과 일치하는지 ?
     */
    public boolean isEmailDomainMatchedWithUniv(Long userId, String email) {
        // 1. 유저 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 유저입니다."));

        // 2. 유저의 소속 대학교 도메인 가져오기
        String universityDomain = user.getUniversity().getEmailDomain(); // 예: "skku.edu"

        // 3. 사용자가 입력한 이메일에서 도메인 추출
        String inputDomain = extractDomain(email); // 예: "skku.edu"

        // 4. 비교 (대소문자 무시)
        return inputDomain.equalsIgnoreCase(universityDomain);
    }

    private String extractDomain(String email) {
        return email.substring(email.indexOf("@") + 1).toLowerCase();
    }

    /**
     * 인증 메일 보내기
     */

}
