package com.runningRank.runningRank.runningRecord.domain;

import com.runningRank.runningRank.user.domain.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(
        name = "running_record",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "type"})
)
public class RunningRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 러닝 타입: TEN_KM, HALF, FULL
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, unique = false)
    private RunningType type;

    // 기록 시간 (예: 초 단위로 저장)
    @Column(nullable = false)
    private int recordTimeInSeconds;

    // 기록 날짜
    private LocalDateTime recordDate;

    // 유저와의 연관관계
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;
}