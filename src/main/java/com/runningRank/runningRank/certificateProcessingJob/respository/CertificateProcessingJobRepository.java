package com.runningRank.runningRank.certificateProcessingJob.respository;

import com.runningRank.runningRank.certificateProcessingJob.domain.CertificateProcessingJob;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CertificateProcessingJobRepository extends JpaRepository<CertificateProcessingJob, Long> {
}
