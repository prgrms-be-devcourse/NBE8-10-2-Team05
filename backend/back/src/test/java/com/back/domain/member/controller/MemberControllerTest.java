package com.back.domain.member.controller;

import static org.assertj.core.api.Assertions.assertThat;
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

import com.back.domain.member.repository.MemberRepository;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class MemberControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private MemberRepository memberRepository;

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

    @Test
    @DisplayName("회원가입 성공 - 200 반환 + DB 저장됨")
    void join_success() throws Exception {
        // given
        String body = """
            {
              "name": "홍길동",
              "email": "test@example.com",
              "password": "12345678",
              "rrnFront": 991231,
              "rrnBackFirst": 1
            }
            """;

        // when & then
        mvc.perform(post("/api/v1/member/join")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.name").value("홍길동"));

        var saved = memberRepository.findByEmail("test@example.com").orElse(null);
        assertThat(saved).isNotNull();
        assertThat(saved.getName()).isEqualTo("홍길동");

        // 비밀번호 해시 저장이면 평문이 아니어야 함
        assertThat(saved.getPassword()).isNotEqualTo("12345678");
    }

    @Test
    @DisplayName("회원가입 실패 - 이메일 중복이면 예외 반환")
    void join_fail_duplicate_email() throws Exception {
        // given: 같은 이메일로 1번 가입
        String body = """
            {
              "name": "홍길동",
              "email": "dup@example.com",
              "password": "12345678",
              "rrnFront": 991231,
              "rrnBackFirst": 1
            }
            """;

        mvc.perform(post("/api/v1/member/join")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk());

        // when: 같은 이메일로 2번 가입
        var result = mvc.perform(post("/api/v1/member/join")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body));

        // then
        result.andExpect(status().isConflict())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.resultCode").value("MEMBER_409"))
                .andExpect(jsonPath("$.msg").value("이미 존재하는 이메일입니다"));
    }

    @Test
    @DisplayName("회원가입 실패 - 잘못된 요청(JSON 필드 누락) 시 400")
    void join_fail_invalid_request() throws Exception {
        // email 누락
        String body = """
            {
              "name": "홍길동",
              "password": "12345678",
              "rrnFront": 991231,
              "rrnBackFirst": 1
            }
            """;

        mvc.perform(post("/api/v1/member/join")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }
}
