package com.runningRank.runningRank.runningRecord.domain;

import com.runningRank.runningRank.user.domain.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(
        name = "running_record",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "runningType"})
)
@EntityListeners(AuditingEntityListener.class)
public class RunningRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 러닝 타입: TEN_KM, HALF, FULL
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, unique = false)
    private RunningType runningType;

    // 기록 시간 (예: 초 단위로 저장)
    @Column(nullable = false)
    private int recordTimeInSeconds;

    // 기록 날짜
    private LocalDateTime recordDate;

    // 생성일
    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // 유저와의 연관관계
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;
}