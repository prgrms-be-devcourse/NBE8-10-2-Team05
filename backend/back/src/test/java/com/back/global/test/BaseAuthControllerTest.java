package com.back.global.test;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.back.domain.member.entity.Member;
import com.back.domain.member.repository.MemberRepository;
import com.back.global.security.jwt.JwtProvider;

public abstract class BaseAuthControllerTest extends BaseControllerTest {

    @Autowired
    protected JwtProvider jwtProvider;

    @Autowired
    protected MemberRepository memberRepository;

    protected String accessToken;
    protected Long memberId;

    @BeforeEach
    void setUpAuth() {
        // 테스트용 사용자 생성 (팩토리 메서드 사용)

        String testemail = "test" + System.nanoTime() + "@email.com";

        Member member = Member.createEmailUser("테스트이메일", testemail, "encoded_password", 990101, 1);

        member = memberRepository.save(member);
        memberId = member.getId();

        // JWT 발급
        accessToken = jwtProvider.issueAccessToken(
                memberId, member.getEmail(), member.getRole().name());

        // 인증 포함 MockMvc로 교체
        mvc = MockMvcBuilders.webAppContextSetup(context)
                .defaultRequest(get("/").header("Authorization", "Bearer " + accessToken))
                .build();
    }
}
