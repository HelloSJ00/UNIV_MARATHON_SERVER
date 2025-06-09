package com.runningRank.runningRank.user.domain;

import jakarta.persistence.*;

@Entity
public class User {

    /**
     *  1. PK
     *  2. 계정
     *  3. 비밀번호
     *  + 3 - 2. 소셜 로그인용 outhProvider
     *  + 3 - 3. 소셜 로그인용 outhId
     *  4. 이름
     *  5. 나이
     *  6. 성별
     *  7. 학교
     *  8. 학번
     *  9. 전공
     *  10. 프로필 이미지
     *  11. USER_ROLE
     */

    /**
     * 추후 추가할 것들
     * 1. 뱃지
     */

    // 1
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 2
    @Column(nullable = false, unique = true)
    private String email;

    // 3
    private String password;

    // 3 - 2
    private String oauthProvider;

    // 3 - 3
    private String oauthId;

    // 4
    @Column(nullable = false)
    private String name;

    // 5
    private int age;

    // 6
    @Enumerated(EnumType.STRING)
    private Gender gender;

    // 7
    @Enumerated(EnumType.STRING)
    private School school;

    // 8
    private String studentNumber;

    // 9
    private String major;

    // 10
    private String profileImageUrl;

    // 11
    private Role role;
}
