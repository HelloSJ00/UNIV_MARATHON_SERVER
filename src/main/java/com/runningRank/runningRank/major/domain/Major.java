package com.runningRank.runningRank.major.domain;

import jakarta.persistence.*;
import lombok.Getter;

@Entity
@Getter
public class Major {

    /**
     * 1. PK
     * 2. 전공명
     * 3. 학교
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name; // 전공명

    private String universityName; // 전공이 소속된 학교
}
