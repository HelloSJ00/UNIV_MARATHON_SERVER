package com.runningRank.runningRank.user.domain;

public enum School {

    /**
     * 경기도
     */

    /**
     * 충청도
     */

    /**
     * 강원도
     */

    /**
     * 경상도
     */

    /**
     * 전라도
     */

    /**
     * 제주도
     */

    /**
     * 서울시 대학교
     */
    CATHOLIC("가톨릭대학교"),
    METHODIST("감리교신학대학교"),
    KONKUK("건국대학교"),
    KYONGGI("경기대학교"),
    KYUNGHEE("경희대학교"),
    KOREA("고려대학교"),
    KWANGWOON("광운대학교"),
    KOOKMIN("국민대학교"),
    CHRISTIAN("그리스도신학대학교"),
    DANKOOK("단국대학교"),
    DUKSUNG("덕성여자대학교"),
    DONGGUK("동국대학교"),
    DONGDUK("동덕여자대학교"),
    MYONGJI("명지대학교"),
    SAMYOOK("삼육대학교"),
    SANGMYUNG("상명대학교"),
    SOGANG("서강대학교"),
    SUKYUNG("서경대학교"),
    SEOUL_CHRISTIAN("서울기독대학교"),
    SEOUL("서울대학교"),
    UOS("서울시립대학교"),
    SEOUL_WOMEN("서울여자대학교"),
    ANGLICAN("성공회대학교"),
    SUNGKYUNKWAN("성균관대학교"),
    SUNGSHIN("성신여자대학교"),
    SEJONG("세종대학교"),
    SUKMYUNG("숙명여자대학교"),
    SOONGSIL("숭실대학교"),
    YONSEI("연세대학교"),
    EWHA("이화여자대학교"),
    PRESBYTERIAN("장로회신학대학교"),
    CHUNGANG("중앙대학교"),
    CHONGSHIN("총신대학교"),
    CHUGYE("추계예술대학교"),
    KOREAN_BIBLE("한국성서대학교"),
    HUFS("한국외국어대학교"),
    KNSU("한국체육대학교"),
    HANSUNG("한성대학교"),
    HANYANG("한양대학교"),
    HANYOUNG("한영신학대학교"),
    HONGIK("홍익대학교"),
    JUNGANG_BUDDHIST("중앙승가대학교");

    private final String displayName;

    School(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
