package com.runningRank.runningRank.recordVerification.repository;

import com.runningRank.runningRank.emailVerification.domain.VerificationStatus;
import com.runningRank.runningRank.recordVerification.domain.RecordVerification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RecordVerificationRepository extends JpaRepository<RecordVerification,Long> {
    Page<RecordVerification> findByStatus(VerificationStatus status, Pageable pageable);
}
