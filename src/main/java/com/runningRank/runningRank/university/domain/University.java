package com.runningRank.runningRank.university.domain;

import jakarta.persistence.*;
import lombok.Getter;

@Entity
@Getter
public class University {

    /**
     * 1. PK
     * 2. 대학명
     * 3. 대학별 이메일 도메인 형식
     */

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String universityName;

    private String emailDomain;
}
