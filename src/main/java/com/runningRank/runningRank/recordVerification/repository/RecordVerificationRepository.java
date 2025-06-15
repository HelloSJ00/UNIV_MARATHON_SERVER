package com.runningRank.runningRank.recordVerification.repository;

import com.runningRank.runningRank.recordVerification.domain.RecordVerification;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RecordVerificationRepository extends JpaRepository<RecordVerification,Long> {
}
