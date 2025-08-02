package com.runningRank.runningRank.recordUploadLog.repository;

import com.runningRank.runningRank.recordUploadLog.domain.RecordUploadLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RecordUploadLogRepository extends JpaRepository<RecordUploadLog, Long> {
    Optional<RecordUploadLog> findByUserId(Long userId);
}
