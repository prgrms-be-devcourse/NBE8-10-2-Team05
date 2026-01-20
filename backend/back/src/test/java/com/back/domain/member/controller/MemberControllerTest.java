package com.back.domain.member.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class MemberControllerTest {

    @Autowired
    private MockMvc mvc;

    @Test
    @DisplayName("로그인 성공 - 200 반환 + memberId/name 반환")
    void login_success() throws Exception {
        // given: 회원 1명 가입(회원가입 API 호출로 세팅해도 되고, repo로 직접 넣어도 됨)
        String joinBody = """
        {
          "name": "홍길동",
          "email": "login_test@example.com",
          "password": "12345678",
          "rrnFront": 991231,
          "rrnBackFirst": 1
        }
        """;

        mvc.perform(post("/api/v1/member/join")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(joinBody))
                .andExpect(status().isOk());

        String loginBody = """
        {
          "email": "login_test@example.com",
          "password": "12345678"
        }
        """;

        // when & then
        mvc.perform(post("/api/v1/member/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginBody))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.memberId").exists())
                .andExpect(jsonPath("$.name").value("홍길동"));
    }

    @Test
    @DisplayName("로그인 실패 - 비밀번호 불일치면 4xx 반환")
    void login_fail_wrong_password() throws Exception {
        // given
        String joinBody = """
        {
          "name": "홍길동",
          "email": "wrong_pw@example.com",
          "password": "12345678",
          "rrnFront": 991231,
          "rrnBackFirst": 1
        }
        """;

        mvc.perform(post("/api/v1/member/join")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(joinBody))
                .andExpect(status().isOk());

        String loginBody = """
        {
          "email": "wrong_pw@example.com",
          "password": "WRONG_PASSWORD"
        }
        """;

        mvc.perform(post("/api/v1/member/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginBody))
                .andExpect(status().is4xxClientError());
    }

    @Test
    @DisplayName("로그인 실패 - 존재하지 않는 이메일이면 4xx 반환")
    void login_fail_email_not_found() throws Exception {
        // given
        String loginBody = """
        {
          "email": "notfound@example.com",
          "password": "12345678"
        }
        """;

        mvc.perform(post("/api/v1/member/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginBody))
                .andExpect(status().is4xxClientError());
    }
}
