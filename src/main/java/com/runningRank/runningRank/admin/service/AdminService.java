package com.runningRank.runningRank.admin.service;

import com.runningRank.runningRank.admin.dto.RecordVerificationInfo;
import com.runningRank.runningRank.emailVerification.domain.VerificationStatus;
import com.runningRank.runningRank.recordVerification.domain.RecordVerification;
import com.runningRank.runningRank.recordVerification.repository.RecordVerificationRepository;
import com.runningRank.runningRank.runningRecord.domain.RunningRecord;
import com.runningRank.runningRank.runningRecord.domain.RunningType;
import com.runningRank.runningRank.runningRecord.repository.RunningRecordRepository;
import com.runningRank.runningRank.user.domain.User;
import com.runningRank.runningRank.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminService {

    private final RecordVerificationRepository recordVerificationRepository;
    private final UserRepository userRepository;
    private final RunningRecordRepository runningRecordRepository;

    public Page<RecordVerificationInfo> getPendingRecordVerifications(Pageable pageable) {
        return recordVerificationRepository.findByStatus(VerificationStatus.PENDING, pageable)
                .map(record -> RecordVerificationInfo.builder()
                        .userId(record.getUser().getId())
                        .recordVerificationId(record.getId())
                        .imageUrl(record.getImageUrl())
                        .marathonName(record.getMarathonName())
                        .runningType(record.getRunningType())
                        .recordTime(record.getRecordTime())
                        .status(record.getStatus())
                        .build());
    }

    @Transactional
    public boolean confirmRecordVerification(Long userId, Long recordVerificationId) {
        try {
            // 1. 객체 조회
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new IllegalArgumentException("해당 유저가 존재하지 않습니다."));

            RecordVerification recordVerification = recordVerificationRepository.findById(recordVerificationId)
                    .orElseThrow(() -> new IllegalArgumentException("해당 기록 검증 요청이 존재하지 않습니다."));

            // 2. 이미 승인된 기록이라면 중복 저장 방지
            if (recordVerification.getStatus() == VerificationStatus.VERIFIED) {
                log.warn("이미 검증된 기록입니다. ID: {}", recordVerificationId);
                return false;
            }

            // 기존 기록이 있다면 수정
            Optional<RunningRecord> existingRecordOpt = runningRecordRepository
                    .findByUserIdAndRunningType(userId, recordVerification.getRunningType());

            if (existingRecordOpt.isPresent()) {
                RunningRecord existing = existingRecordOpt.get();
                existing.updateRecord(recordVerification.getMarathonName(),recordVerification.getRecordTime()); // 이 메서드는 직접 정의해야 함
            } else {
                RunningRecord newRecord = RunningRecord.builder()
                        .runningType(recordVerification.getRunningType())
                        .marathonName(recordVerification.getMarathonName())
                        .recordTimeInSeconds(recordVerification.getRecordTime())
                        .createdAt(LocalDateTime.now())
                        .user(user)
                        .build();
                runningRecordRepository.save(newRecord);
            }

            // 5. 검증 상태 변경
            recordVerification.changeStatus(VerificationStatus.VERIFIED);

            return true;

        } catch (Exception e) {
            log.error("기록 검증 승인 중 오류 발생", e);
            return false;
        }
    }

    @Transactional
    public boolean rejectRecordVerification(Long recordVerificationId) {
        try {
            RecordVerification recordVerification = recordVerificationRepository.findById(recordVerificationId)
                    .orElseThrow(() -> new IllegalArgumentException("해당 기록 검증 요청이 존재하지 않습니다."));

            // 이미 처리된 상태면 중복 방지
            if (recordVerification.getStatus() != VerificationStatus.PENDING) {
                log.warn("이미 처리된 기록입니다. ID: {}", recordVerificationId);
                return false;
            }

            // 상태를 REJECTED 또는 EXPIRED로 변경
            recordVerification.changeStatus(VerificationStatus.EXPIRED);
            return true;

        } catch (Exception e) {
            log.error("기록 검증 거절 중 오류 발생", e);
            return false;
        }
    }
}
