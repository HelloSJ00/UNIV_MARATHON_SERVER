package com.runningRank.runningRank.recordVerification.exception;

public class CallQuotaExceededException extends RuntimeException {
    public CallQuotaExceededException() {
        super("이번 달 호출 가능 횟수를 모두 사용했습니다. 다음 달에 다시 시도해주세요.");
    }
}
