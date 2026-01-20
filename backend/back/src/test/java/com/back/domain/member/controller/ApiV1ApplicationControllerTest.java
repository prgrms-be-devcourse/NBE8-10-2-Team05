package com.back.domain.member.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.back.domain.member.entity.Member;
import com.back.domain.member.repository.MemberRepository;
import com.back.domain.welfare.policy.entity.Policy;
import com.back.domain.welfare.policy.repository.PolicyRepository;
import com.back.standard.util.Ut;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class ApiV1ApplicationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ApplicationController applicationController;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private PolicyRepository policyRepository;

    private static String JWT_SECRET_KEY;

    @Value("${custom.jwt.secretKey}")
    void setJwtSecretKey(String key) {
        JWT_SECRET_KEY = key;
    }

    private static final int JWT_EXPIRE_SECONDS = 60 * 20; // 20분

    @Test
    @DisplayName("신청내역 추가 - 200 + AddApplicationResponseDto 반환")
    void addApplicationSuccessTest() throws Exception {
        // given: Member 생성 및 저장
        Member member = Member.createEmailUser("홍길동", "test@example.com", "encodedPassword123", 991231, 1);
        Member savedMember = memberRepository.save(member);

        // given: Policy 생성 및 저장 (리플렉션 사용)
        Policy policy = new Policy();
        try {
            Field plcyNoField = Policy.class.getDeclaredField("plcyNo");
            plcyNoField.setAccessible(true);
            plcyNoField.set(policy, "TEST-POLICY-001");

            Field plcyNmField = Policy.class.getDeclaredField("plcyNm");
            plcyNmField.setAccessible(true);
            plcyNmField.set(policy, "테스트 정책");
        } catch (Exception e) {
            throw new RuntimeException("Policy 필드 설정 실패", e);
        }
        Policy savedPolicy = policyRepository.save(policy);

        // given: JWT 토큰 생성
        Map<String, Object> jwtBody = new HashMap<>();
        jwtBody.put("memberId", savedMember.getId());
        String jwt = Ut.Jwt.toString(JWT_SECRET_KEY, JWT_EXPIRE_SECONDS, jwtBody);

        // when & then: POST 요청 보내고 정상적인 응답 확인
        mockMvc.perform(post("/api/v1/member/welfare-application/" + savedPolicy.getId())
                        .header("Authorization", "Bearer " + jwt)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("저장되었습니다!"));
    }

    // TODO: 1. 신청 내역 추가하기 (실패 버전)

    // TODO: 2. 신청 내역 조회하기 (성공/실패 버전)

    // TODO: 3. 신청 내역 삭제하기 (성공/실패 버전)
}
