package com.runningRank.runningRank.university.domain;

import jakarta.persistence.*;

@Entity
public class University {

    /**
     * 1. PK
     * 2. 대학명
     * 3. 대학별 이메일 도메인 형식
     */

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String universityName;

    @Column(unique = true)
    private String emailDomain;
}
