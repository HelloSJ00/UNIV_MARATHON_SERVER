package com.runningRank.runningRank.emailVerification.service;

import com.runningRank.runningRank.emailVerification.domain.EmailVerification;
import com.runningRank.runningRank.emailVerification.domain.VerificationStatus;
import com.runningRank.runningRank.emailVerification.repository.EmailVerificationRepository;
import com.runningRank.runningRank.user.domain.User;
import com.runningRank.runningRank.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailVerificationService {

    private final UserRepository userRepository;
    private final EmailVerificationRepository emailVerificationRepository;
    private final JavaMailSender mailSender;


    /**
     * 이메일 도메인이 유저 학교의 도메인과 일치하는지 ?
     * @param userId
     * @param univEmail
     * @return
     */
    public boolean isEmailDomainMatchedWithUniv(Long userId, String univEmail) {
        // 1. 유저 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 유저입니다."));

        // 2. 유저의 소속 대학교 도메인 가져오기
        String universityDomain = user.getUniversity().getEmailDomain(); // 예: "skku.edu"
        log.debug("유저 대학교 이메일 도메인 :" + universityDomain);
        // 3. 사용자가 입력한 이메일에서 도메인 추출
        String inputDomain = extractDomain(univEmail); // 예: "skku.edu"
        log.debug("유저가 입력한 도메인에서 추출된 도메인 :" + inputDomain);
        // 4. 비교 (대소문자 무시)
        return inputDomain.endsWith(universityDomain);
    }

    private String extractDomain(String univEmail) {
        int atIndex = univEmail.indexOf("@");
        if (atIndex == -1 || atIndex == univEmail.length() - 1) {
            throw new IllegalArgumentException("올바르지 않은 이메일 형식입니다.");
        }
        return univEmail.substring(atIndex + 1).toLowerCase(); // 예: g.skku.edu
    }


    /**
     * 인증 메일 보내기
     * @param univEmail
     * @return 성공 시 true
     */
    public boolean sendVerificationCode(String univEmail) {
        try {
            // 새 인증 코드 생성
            String code = generateRandomCode();

            // 기존 PENDING 인증이 있으면 무효화
            List<EmailVerification> previous = emailVerificationRepository.findByEmailAndStatus(univEmail, VerificationStatus.PENDING);
            for (EmailVerification v : previous) {
                v.changeStatus(VerificationStatus.EXPIRED);
            }

            // 새 인증 객체 생성
            EmailVerification verification = EmailVerification.builder()
                    .email(univEmail)
                    .code(code)
                    .createdAt(LocalDateTime.now())
                    .status(VerificationStatus.PENDING)
                    .build();

            // 새 인증 객체 저장
            emailVerificationRepository.save(verification);

            // 인증 이메일 전송
            sendEmail(univEmail, code);

            return true;
        } catch (Exception e) {
            // 로그 찍기 등 예외 처리
            log.error("이메일 인증 코드 전송 실패: {}", e.getMessage());
            return false;
        }
    }
    private void sendEmail(String to, String code) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("[러닝랭크] 이메일 인증 코드");
        message.setText("아래 코드를 입력해주세요:\n\n" + code);
        mailSender.send(message);
    }
    private String generateRandomCode() {
        return String.valueOf(new Random().nextInt(900000) + 100000); // 6자리 숫자
    }


    /**
     * 인증 코드 검증 로직
     * @param univEmail
     * @param inputCode
     */
    public boolean verifyCode(String univEmail, String inputCode) {
        try{
            EmailVerification verification = emailVerificationRepository
                    .findTopByEmailAndStatusOrderByCreatedAtDesc(univEmail, VerificationStatus.PENDING)
                    .orElseThrow(() -> new IllegalArgumentException("인증 요청이 없습니다."));

            if (verification.isExpired()) {
                verification.changeStatus(VerificationStatus.EXPIRED);
                emailVerificationRepository.save(verification);
                throw new IllegalStateException("인증 코드가 만료되었습니다.");
            }

            if (!verification.isCodeMatched(inputCode)) {
                throw new IllegalArgumentException("인증 코드가 일치하지 않습니다.");
            }

            // 인증 성공
            verification.changeStatus(VerificationStatus.VERIFIED);
            emailVerificationRepository.save(verification);

            User user = userRepository.findByEmail(univEmail)
                    .orElseThrow(() -> new IllegalArgumentException("유저를 찾을 수 없습니다."));

            user.verifyUnivEmail(univEmail); // email 세팅, isUniversityVerified = true
            userRepository.save(user);
            return true;
        } catch (Exception e) {
            // 로그 찍기 등 예외 처리
            log.error("이메일 인증 코드 확인 실패: {}", e.getMessage());
            return false;
        }

    }
}
