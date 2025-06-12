package com.runningRank.runningRank.emailVerification.service;

import com.runningRank.runningRank.university.repository.UniversityRepository;
import com.runningRank.runningRank.user.domain.User;
import com.runningRank.runningRank.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
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
        log.debug("유저 대학교 이메일 도메인 :" + universityDomain);
        // 3. 사용자가 입력한 이메일에서 도메인 추출
        String inputDomain = extractDomain(email); // 예: "skku.edu"
        log.debug("유저가 입력한 도메인에서 추출된 도메인 :" + inputDomain);
        // 4. 비교 (대소문자 무시)
        return inputDomain.endsWith(universityDomain);
    }

    private String extractDomain(String email) {
        int atIndex = email.indexOf("@");
        if (atIndex == -1 || atIndex == email.length() - 1) {
            throw new IllegalArgumentException("올바르지 않은 이메일 형식입니다.");
        }
        return email.substring(atIndex + 1).toLowerCase(); // 예: g.skku.edu
    }

    /**
     * 인증 메일 보내기
     */

}
