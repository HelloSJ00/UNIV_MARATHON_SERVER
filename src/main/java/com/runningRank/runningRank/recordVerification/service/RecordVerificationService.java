package com.runningRank.runningRank.recordVerification.service;

import com.runningRank.runningRank.recordVerification.repository.RecordVerificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class RecordVerificationService {

    private final RecordVerificationRepository recordVerificationRepository;

    /**
     * 검증 기록 데이터 만들기
     */
    public void createRecordVerification(@Param("s3ImageUrl") String s3ImageUrl){

    }
}
