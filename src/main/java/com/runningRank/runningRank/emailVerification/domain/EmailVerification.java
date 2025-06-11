package com.runningRank.runningRank.emailVerification.domain;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
public class EmailVerification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String email;
    private String code;
    private LocalDateTime createdAt;

    @Enumerated(EnumType.STRING)
    private VerificationStatus status; // PENDING, VERIFIED, EXPIRED

    public boolean isExpired() {
        return createdAt.plusMinutes(10).isBefore(LocalDateTime.now());
    }
}