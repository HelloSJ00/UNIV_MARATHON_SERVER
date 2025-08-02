package com.runningRank.runningRank.recordUploadLog.service;

import com.runningRank.runningRank.recordUploadLog.domain.RecordUploadLog;
import com.runningRank.runningRank.recordUploadLog.repository.RecordUploadLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class RecordUploadLogService {

    private final RecordUploadLogRepository recordUploadLogRepository;

    public boolean increaseCallCount(Long userId) {
        RecordUploadLog recordUploadLog = recordUploadLogRepository.findByUserId(userId)
                .orElseGet(() -> new RecordUploadLog(userId));

        recordUploadLog.increaseCallCount();
        recordUploadLogRepository.save(recordUploadLog);
        return true;
    }

    public boolean checkUserCanCall(Long userId) {
        return recordUploadLogRepository.findByUserId(userId)
                .map(log -> log.getCallCount() < 3)
                .orElse(true);  // 없으면 처음 호출하는 거니까 true
    }
}
