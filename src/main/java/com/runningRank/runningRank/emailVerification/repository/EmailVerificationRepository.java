package com.runningRank.runningRank.emailVerification.repository;

import com.runningRank.runningRank.emailVerification.domain.EmailVerification;
import com.runningRank.runningRank.emailVerification.domain.VerificationStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EmailVerificationRepository extends JpaRepository<EmailVerification,Long> {
    List<EmailVerification> findByEmailAndStatus(String email, VerificationStatus verificationStatus);
    Optional<EmailVerification> findTopByEmailAndStatusOrderByCreatedAtDesc(String email,VerificationStatus verificationStatus);
}
