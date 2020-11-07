package com.lutech.cms.config;

import com.lutech.cms.annotation.IgnoreResponseAdvice;
import lombok.Data;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

@RestControllerAdvice
public class ResponseDateAdvice implements ResponseBodyAdvice {
    @Override
    public boolean supports(MethodParameter methodParameter, Class aClass) {
        if (methodParameter.getDeclaringClass().isAnnotationPresent(IgnoreResponseAdvice.class)) {
            return false;
        } else if (methodParameter.getMethod().isAnnotationPresent(IgnoreResponseAdvice.class)) {
            return false;
        } else {
            return true;
        }
    }

    @Override
    public Object beforeBodyWrite(Object body, MethodParameter methodParameter, MediaType mediaType, Class aClass, ServerHttpRequest serverHttpRequest, ServerHttpResponse serverHttpResponse) {
        if (body instanceof Result) {
            return body;
        }

        return Result.success(body);
    }

    @Data
    public static class Result<T> {
        private int code;
        private String message;
        private T data;

        public static <T> Result<T> success(T t) {
            Result<T> result = new Result<>();
            result.code = 200;
            result.message = "ok";
            result.data = t;
            return result;
        }

        public static Result<Object> failed(int code, String message) {
            Result<Object> result = new Result<>();
            result.code = code;
            result.message = message;
            return result;
        }
    }
}

