package com.runningRank.runningRank.recordUploadLog.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.time.YearMonth;
import java.time.format.DateTimeFormatter;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "record_upload_log",uniqueConstraints = @UniqueConstraint(columnNames = {"user_id","upload_time"}))
public class RecordUploadLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;

    private String uploadTime;

    private int callCount;

    // 생성
    public RecordUploadLog(Long userId) {
        this.userId = userId;
        this.uploadTime = YearMonth.now().format(DateTimeFormatter.ofPattern("yyyy-MM"));
        this.callCount = 0;
    }

    // 카운트 ++
    public void increaseCallCount() {
        this.callCount++;
    }
}
