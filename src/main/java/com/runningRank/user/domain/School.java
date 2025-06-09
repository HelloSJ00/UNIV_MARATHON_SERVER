package com.runningRank.user.domain;

public enum School {
    SUNGKYUNKWAN("성균관대학교"),
    SEOUL("서울대학교"),
    KOREA("고려대학교"),
    YONSEI("연세대학교"),
    HANYANG("한양대학교");

    private final String displayName;

    School(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
