package com.fauna.response;

/**
 * This class will encapsulate all the information Fauna returns about errors including constraint failures, and
 * abort data, for now it just has the code and message.
 */
public class ErrorInfo {
    private final String code;
    private final String message;

    public ErrorInfo(String code, String message) {
        this.code = code;
        this.message = message;
    }

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
