package com.lutech.cms.config;

import lombok.Getter;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    public enum ErrorCode {
        SUCCESS(200, "ok"),
        METHODFAIL(2000, "ENCOUNTER AN ERROR WHEN EXECUTE METHOD"),
        UNKNOWEXCEPTION(3000, "THIS IS AN UNKNOW EXCEPTION");

        @Getter
        private int code;
        @Getter
        private String message;

        ErrorCode(int code, String message) {
            this.code = code;
            this.message = message;
        }
    }
}
