package com.runningRank.runningRank.emailVerification.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
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

    public void changeStatus(VerificationStatus verificationStatus){
        status = verificationStatus;
    }

    public boolean isCodeMatched(String inputCode) {
        return this.code.equals(inputCode);
    }
}