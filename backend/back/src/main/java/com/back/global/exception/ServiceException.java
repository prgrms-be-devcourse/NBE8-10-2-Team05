package com.back.global.exception;

import lombok.Getter;

@Getter
public class ServiceException extends RuntimeException {
    private final String resultCode;
    private final String msg;

    public ServiceException(String resultCode, String msg) {
        super(resultCode + " : " + msg);
        this.resultCode = resultCode;
        this.msg = msg;
    }

    public String getLocation() {
        // 여기다.
        StackTraceElement[] stackTrace = this.getStackTrace();
        if (stackTrace != null && stackTrace.length > 0) {
            StackTraceElement top = stackTrace[0];
            // 클래스명에서 패키지는 빼고 이름만 보고 싶다면 top.getFileName()을 써도 좋습니다.
            return String.format("%s.%s:%d", top.getClassName(), top.getMethodName(), top.getLineNumber());
        }
        return "Unknown Location";
    }
}
