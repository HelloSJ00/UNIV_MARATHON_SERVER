package com.runningRank.runningRank.recordVerification.repository;

import com.runningRank.runningRank.emailVerification.domain.VerificationStatus;
import com.runningRank.runningRank.recordVerification.domain.RecordVerification;
import com.runningRank.runningRank.runningRecord.domain.RunningType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface RecordVerificationRepository extends JpaRepository<RecordVerification, Long> {
    Page<RecordVerification> findByStatus(VerificationStatus status, Pageable pageable);
    Optional<RecordVerification >findByUserIdAndRunningTypeAndStatus(Long userId, RunningType runningType,VerificationStatus status);
}
