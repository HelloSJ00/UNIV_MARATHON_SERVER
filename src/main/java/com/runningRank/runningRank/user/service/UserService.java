package com.runningRank.runningRank.user.service;

import com.runningRank.runningRank.auth.dto.UserInfo;
import com.runningRank.runningRank.auth.dto.UserUpdateRequest;
import com.runningRank.runningRank.emailVerification.domain.EmailVerification;
import com.runningRank.runningRank.emailVerification.domain.VerificationStatus;
import com.runningRank.runningRank.emailVerification.repository.EmailVerificationRepository;
import com.runningRank.runningRank.major.domain.Major;
import com.runningRank.runningRank.major.repository.MajorRepository;
import com.runningRank.runningRank.university.domain.University;
import com.runningRank.runningRank.university.repository.UniversityRepository;
import com.runningRank.runningRank.user.domain.User;
import com.runningRank.runningRank.user.dto.PresignedUrlRequest;
import com.runningRank.runningRank.user.dto.PresignedUrlResponse;
import com.runningRank.runningRank.user.dto.UserVerification;
import com.runningRank.runningRank.user.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.net.URI;
import java.time.Duration;
import java.util.List;
import java.util.UUID; // UUID 추가

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final UniversityRepository universityRepository;
    private final MajorRepository majorRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailVerificationRepository emailVerificationRepository;
    private final S3Presigner s3Presigner; // S3Presigner 빈은 별도의 @Configuration에서 정의되어야 합니다.

    @Value("${cloud.aws.s3.bucket}")
    private String bucket; // application.yml 또는 .properties에서 주입받는 S3 버킷 이름

    @Value("${cloud.aws.region.static}")
    private String region;

    // 사용자 인증 관련 메서드 (기존 코드 유지)
    public List<UserVerification> getUserVerifications(Long userId) {
        return userRepository.findGroupedVerification(userId);
    }

    /**
     * 내 정보 수정
     */
    @Transactional
    public UserInfo updateUserInfo(UserUpdateRequest request, Long userId) {
        // 1. 유저 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("해당 유저를 찾을 수 없습니다."));

        // 2. 대학 및 전공 처리
        University newUniversity = null;
        if (request.getUniversityName() != null && !request.getUniversityName().isBlank()) {
            newUniversity = universityRepository.findByUniversityName(request.getUniversityName())
                    .orElseThrow(() -> new EntityNotFoundException("대학교를 찾을 수 없습니다: " + request.getUniversityName()));
        }

        Major newMajor = null;
        if (request.getMajor() != null && !request.getMajor().isBlank()) {
            if (newUniversity == null) {
                throw new IllegalStateException("전공을 찾기 위해선 대학 정보가 필요합니다.");
            }
            newMajor = majorRepository.findByNameAndUniversityName(request.getMajor(), newUniversity.getUniversityName())
                    .orElseThrow(() -> new EntityNotFoundException("해당 대학교에서 전공을 찾을 수 없습니다: " + request.getMajor()));
        }

        // 3. 유저 정보 업데이트
        user.updateInfo(request, newUniversity, newMajor);

        // 4. DTO 응답 변환 및 반환
        return UserInfo.from(user);
    }
    /**
     * 클라이언트가 S3에 직접 파일을 업로드할 수 있도록 Presigned URL을 생성합니다.
     *
     * @param request Presigned URL 생성에 필요한 파일명, 파일 타입 정보를 담은 요청 객체
     * @return 생성된 Presigned URL과 최종 파일 접근 URL을 포함하는 응답 객체
     */
    public PresignedUrlResponse generatePresignedUrl(PresignedUrlRequest request) {
        // 1. S3 객체 키 생성: 파일명에 관계없이 고유하고 안전한 문자열을 사용합니다.
        // 이는 URISyntaxException 방지에 매우 효과적입니다.
        String originalFileName = request.getFileName();
        String fileExtension = "";
        int lastDotIndex = originalFileName.lastIndexOf(".");
        if (lastDotIndex != -1 && lastDotIndex < originalFileName.length() - 1) {
            fileExtension = originalFileName.substring(lastDotIndex); // .jpg, .png 등 확장자 추출
        }
        // "uploads/" 폴더 내에 UUID로 고유한 이름 + 원래 파일 확장자로 저장
        String objectKey = "uploads/" + UUID.randomUUID().toString() + fileExtension;

        // 2. S3 PutObjectRequest 생성: S3에 어떤 객체를 어떤 속성으로 업로드할지 정의
        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucket) // 주입받은 S3 버킷 이름
                .key(objectKey) // 위에서 생성한 고유한 객체 키
                .contentType(request.getFileType()) // 업로드될 파일의 MIME 타입 (예: "image/jpeg")
                .build();

        // 3. PutObjectPresignRequest 생성: 생성된 PutObjectRequest를 기반으로 Presigned URL 요청 정의
        PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
                .putObjectRequest(putObjectRequest) // PutObjectRequest 객체 포함
                .signatureDuration(Duration.ofMinutes(5)) // Presigned URL의 유효 시간 (예: 5분)
                .build();

        // 4. Presigned URL 생성: S3Presigner를 사용하여 실제 Presigned URL 얻기
        // .url().toURI()는 AWS SDK의 표준 방식이며, objectKey가 안전하게 생성되면 URISyntaxException 방지
        URI presignedUrl = null;
        try {
            presignedUrl = s3Presigner.presignPutObject(presignRequest).url().toURI();
        } catch (java.net.URISyntaxException e) {
            // URISyntaxException 발생 시 로그 기록 및 예외 처리
            // (예: 사용자 정의 예외로 변환하여 ControllerAdvice에서 처리)
            System.err.println("URISyntaxException 발생: " + e.getMessage());
            // 실제 애플리케이션에서는 RuntimeException을 던지거나, 사용자 정의 예외로 변환하여 처리
            throw new RuntimeException("Presigned URL 생성 중 URL 문법 오류 발생", e);
        }

        // 5. 최종 파일 접근 URL 생성: 파일이 S3에 업로드된 후 직접 접근할 수 있는 공개 URL
        // `region` 변수를 사용하여 URL을 동적으로 생성
        String fileUrl = "https://" + bucket + ".s3." + region + ".amazonaws.com/" + objectKey;

        // 6. 응답 객체 반환
        return new PresignedUrlResponse(presignedUrl.toString(), fileUrl);
    }

    /**
     * 비번찾기 인증 코드 검증 로직
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
            return true;
        } catch (Exception e) {
            // 로그 찍기 등 예외 처리
            log.error("이메일 인증 코드 확인 실패: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 비밀번호 바꾸기
     * @param email
     * @param newPassword
     * @return
     */
    @Transactional
    public boolean changeUserPassword(String email, String newPassword) {
        log.info("비밀번호 변경 요청 - email: {}", email);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.warn("비밀번호 변경 실패 - 존재하지 않는 이메일: {}", email);
                    return new IllegalArgumentException("존재하지 않는 사용자입니다.");
                });

        String encodedPassword = passwordEncoder.encode(newPassword);
        user.changePassword(encodedPassword);

        log.info("비밀번호 변경 성공 - userId: {}, email: {}", user.getId(), email);

        return true;
    }

}