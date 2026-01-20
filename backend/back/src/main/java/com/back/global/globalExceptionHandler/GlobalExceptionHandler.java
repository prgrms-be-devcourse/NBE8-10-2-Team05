package com.back.global.globalExceptionHandler;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.back.global.exception.ServiceException;

import lombok.RequiredArgsConstructor;

@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {
    @ExceptionHandler(ServiceException.class)
    public ResponseEntity<Map<String, Object>> handle(ServiceException e) {

        HttpStatus status = HttpStatus.BAD_REQUEST;
        String code = e.getResultCode();

        if (code.contains("401")) status = HttpStatus.UNAUTHORIZED;
        else if (code.contains("404")) status = HttpStatus.NOT_FOUND;
        else if (code.contains("409")) status = HttpStatus.CONFLICT;

        return ResponseEntity.status(status).body(Map.of("resultCode", code, "msg", e.getMsg()));
    }
}
