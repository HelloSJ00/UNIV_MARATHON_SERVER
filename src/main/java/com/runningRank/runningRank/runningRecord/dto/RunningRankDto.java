package com.runningRank.runningRank.runningRecord.dto;

import lombok.Getter;

import com.runningRank.runningRank.runningRecord.domain.RunningType; // RunningType 임포트 필요

@Getter
public class RunningRankDto {

    // 쿼리에서 직접 가져오는 필드들
    private RunningType type; // rr.running_type
    private String marathonName; // rr.marathon_name
    private int recordTimeInSeconds; // rr.record_time_in_seconds

    private Long userId; // u.user_id
    private String name; // u.name
    private String email; // u.email
    private String gender; // u.gender
    private String universityName; // uni.university_name
    private String studentNumber; // u.studnetNumber (오타 주의: studentNumber)
    private String profileImageUrl; // u.profileImage
    private String majorName; // m.name
    private boolean isNameVisible;
    private boolean isStudentNumberVisible;
    private boolean isMajorVisible;
    private String graduationStatus;
    // 쿼리 결과에 없는 필드는 제거하거나, 아래와 같이 나중에 설정하는 방식으로 변경
    private int rank;
    private int totalCount;

    // --- 네이티브 쿼리 결과에 매핑될 생성자를 명시적으로 정의 ---
    // SQL 쿼리의 SELECT 절 순서에 맞춰 파라미터 정의
    public RunningRankDto(
            String runningType,          // 1. OK: String (DB String)
            String marathonName,         // 2. OK: String (DB String)
            int recordTimeInSeconds,     // 3. OK: int (DB int. NULL 없음 확인)
            Long userId,                 // 5. OK: Long (DB long)
            String userName,             // 6. OK: String (DB String)
            String userEmail,            // 7. OK: String (DB String)
            String userGender,           // 8. OK: String (DB String)
            String universityName,       // 9. OK: String (DB String)
            String studentNumber,        // 10. OK: String (DB String/null)
            String profileImage,         // 11. OK: String (DB String/null)
            String majorName             // 12. OK: String (DB String)
    ) {
        this.type = RunningType.valueOf(runningType); // **문제 가능성: String -> Enum 변환 실패 (대소문자/오타)**
        this.marathonName = marathonName;
        this.recordTimeInSeconds = recordTimeInSeconds;
        this.userId = userId;
        this.name = userName;
        this.email = userEmail;
        this.gender = userGender; // Enum으로 변환 필요 시 문제 가능성
        this.universityName = universityName;
        this.studentNumber = studentNumber;
        this.profileImageUrl = profileImage;
        this.majorName = majorName;
        this.rank = 0;
        this.totalCount = 0;
    }

    // `rank`와 `totalCount`를 나중에 설정할 수 있도록 setter 추가 (필요시)
    public void setRank(int rank) {
        this.rank = rank;
    }

    public void setTotalCount(int totalCount) {
        this.totalCount = totalCount;
    }
}