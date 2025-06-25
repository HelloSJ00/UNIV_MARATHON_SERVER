package com.runningRank.runningRank.universityRanking.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter // 모든 필드에 대한 getter 자동 생성
@Setter // (선택 사항) 필요하다면 setter도 추가
@NoArgsConstructor // 기본 생성자 자동 생성
@AllArgsConstructor // 모든 필드를 인자로 받는 생성자 자동 생성
public class FinisherUnivRanking {
    int ranking;
    String universityName;
    String universityImage;
}

