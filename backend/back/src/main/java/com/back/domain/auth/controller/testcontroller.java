package com.back.domain.auth.controller;

import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpSession;

// 테스트용 컨트롤러
@RestController
public class testcontroller {

    @GetMapping("/")
    public String home() {
        return "backend root ok";
    }

    @GetMapping("/session")
    @Operation(summary = "세션 확인")
    public Map<String, Object> session(HttpSession session) {
        return Collections.list(session.getAttributeNames()).stream()
                .collect(Collectors.toMap(name -> name, session::getAttribute));
    }
}
