package com.back.domain.member.policyaply.controller;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
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

import com.back.domain.member.member.entity.Member;
import com.back.domain.member.member.repository.MemberRepository;
import com.back.domain.member.policyaply.entity.Application;
import com.back.domain.member.policyaply.repository.ApplicationRepository;
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

    @Autowired
    private ApplicationRepository applicationRepository;

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

        // given: Policy 생성 및 저장
        Policy policy = createTestPolicy("TEST-POLICY-001", "테스트 정책");
        Policy savedPolicy = policyRepository.save(policy);

        // given: JWT 토큰 생성
        Map<String, Object> jwtBody = new HashMap<>();
        jwtBody.put("memberId", savedMember.getId());
        String jwt = Ut.Jwt.toString(JWT_SECRET_KEY, JWT_EXPIRE_SECONDS, jwtBody);

        // when & then: POST 요청 보내고 정상적인 응답 확인
        mockMvc.perform(post("/api/v1/member/policy-aply/welfare-application/" + savedPolicy.getId())
                        .header("Authorization", "Bearer " + jwt)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("저장되었습니다!"));
    }

    @Test
    @DisplayName("신청내역 추가 실패 - 존재하지 않는 Policy ID로 추가 시도 404 + AddApplicationResponseDto 반환")
    void addApplicationFailInvalidPolicyIdTest() throws Exception {
        // given: Member 생성 및 저장
        Member member = Member.createEmailUser("홍길동", "test@example.com", "encodedPassword123", 991231, 1);
        Member savedMember = memberRepository.save(member);

        // given: 존재하지 않는 Policy ID
        Integer nonExistentPolicyId = 99999;

        // given: JWT 토큰 생성
        Map<String, Object> jwtBody = new HashMap<>();
        jwtBody.put("memberId", savedMember.getId());
        String jwt = Ut.Jwt.toString(JWT_SECRET_KEY, JWT_EXPIRE_SECONDS, jwtBody);

        // when & then: POST 요청 보내고 404 응답 확인
        mockMvc.perform(post("/api/v1/member/policy-aply/welfare-application/" + nonExistentPolicyId)
                        .header("Authorization", "Bearer " + jwt)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("존재하지 않는 정책입니다."));
    }

    @Test
    @DisplayName("신청내역 추가 실패 - JWT 토큰이 없는 경우 401 반환")
    void addApplicationFailNoJwtTest() throws Exception {
        // given: Policy 생성 및 저장
        Policy policy = createTestPolicy("TEST-POLICY-001", "테스트 정책");
        Policy savedPolicy = policyRepository.save(policy);

        // when & then: JWT 없이 POST 요청 보내고 401 응답 확인
        mockMvc.perform(post("/api/v1/member/policy-aply/welfare-application/" + savedPolicy.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.message").value("로그인 후 이용해주세요"));
    }

    @Test
    @DisplayName("신청내역 조회 실패 - JWT 토큰이 없는 경우 401 반환")
    void getApplicationsFailNoJwtTest() throws Exception {
        // when & then: JWT 없이 GET 요청 보내고 401 응답 확인
        mockMvc.perform(get("/api/v1/member/policy-aply/welfare-applications").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.message").value("로그인 후 이용해주세요"));
    }

    @Test
    @DisplayName("신청내역 삭제 실패 - JWT 토큰이 없는 경우 401 반환")
    void deleteApplicationFailNoJwtTest() throws Exception {
        // given: 존재하지 않는 Application ID
        Long nonExistentApplicationId = 99999L;

        // when & then: JWT 없이 PUT 요청 보내고 401 응답 확인
        mockMvc.perform(put("/api/v1/member/policy-aply/welfare-application/" + nonExistentApplicationId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value(401))
                .andExpect(jsonPath("$.message").value("로그인 후 이용해주세요"));
    }

    @Test
    @DisplayName("신청내역 조회 성공 - 신청 내역이 있는 경우 200 + ApplicationList 반환")
    void getApplicationsSuccessTest() throws Exception {
        // given: Member 생성 및 저장
        Member member = Member.createEmailUser("홍길동", "test@example.com", "encodedPassword123", 991231, 1);
        Member savedMember = memberRepository.save(member);

        // given: Policy 생성 및 저장
        Policy policy = createTestPolicy("TEST-POLICY-001", "테스트 정책");
        Policy savedPolicy = policyRepository.save(policy);

        // given: Application 생성 및 저장
        Application application = new Application();
        application.setPolicy(savedPolicy);
        application.setApplicant(savedMember);
        applicationRepository.save(application);

        // given: JWT 토큰 생성
        Map<String, Object> jwtBody = new HashMap<>();
        jwtBody.put("memberId", savedMember.getId());
        String jwt = Ut.Jwt.toString(JWT_SECRET_KEY, JWT_EXPIRE_SECONDS, jwtBody);

        // when & then: GET 요청 보내고 정상적인 응답 확인
        mockMvc.perform(get("/api/v1/member/policy-aply/welfare-applications")
                        .header("Authorization", "Bearer " + jwt)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").exists())
                .andExpect(jsonPath("$[0].policy").exists())
                .andExpect(jsonPath("$[0].applicant").exists())
                .andDo(print());
    }

    @Test
    @DisplayName("신청내역 조회 성공 - 신청 내역이 없는 경우 200 + 빈 리스트 반환")
    void getApplicationsEmptyListTest() throws Exception {
        // given: Member 생성 및 저장
        Member member = Member.createEmailUser("홍길동", "test@example.com", "encodedPassword123", 991231, 1);
        Member savedMember = memberRepository.save(member);

        // given: JWT 토큰 생성
        Map<String, Object> jwtBody = new HashMap<>();
        jwtBody.put("memberId", savedMember.getId());
        String jwt = Ut.Jwt.toString(JWT_SECRET_KEY, JWT_EXPIRE_SECONDS, jwtBody);

        // when & then: GET 요청 보내고 빈 리스트 반환 확인
        mockMvc.perform(get("/api/v1/member/policy-aply/welfare-applications")
                        .header("Authorization", "Bearer " + jwt)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    @DisplayName("신청내역 삭제 성공 - 본인의 신청 내역 삭제 200 + DeleteApplicationResponseDto 반환")
    void deleteApplicationSuccessTest() throws Exception {
        // given: Member 생성 및 저장
        Member member = Member.createEmailUser("홍길동", "test@example.com", "encodedPassword123", 991231, 1);
        Member savedMember = memberRepository.save(member);

        // given: Policy 생성 및 저장
        Policy policy = createTestPolicy("TEST-POLICY-001", "테스트 정책");
        Policy savedPolicy = policyRepository.save(policy);

        // given: Application 생성 및 저장
        Application application = new Application();
        application.setPolicy(savedPolicy);
        application.setApplicant(savedMember);
        Application savedApplication = applicationRepository.save(application);

        // given: JWT 토큰 생성
        Map<String, Object> jwtBody = new HashMap<>();
        jwtBody.put("memberId", savedMember.getId());
        String jwt = Ut.Jwt.toString(JWT_SECRET_KEY, JWT_EXPIRE_SECONDS, jwtBody);

        // when & then: PUT 요청 보내고 정상적인 응답 확인
        mockMvc.perform(put("/api/v1/member/policy-aply/welfare-application/" + savedApplication.getId())
                        .header("Authorization", "Bearer " + jwt)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("성공적으로 삭제되었습니다."));
    }

    @Test
    @DisplayName("신청내역 삭제 실패 - 존재하지 않는 신청 내역 삭제 404 + DeleteApplicationResponseDto 반환")
    void deleteApplicationNotFoundTest() throws Exception {
        // given: Member 생성 및 저장
        Member member = Member.createEmailUser("홍길동", "test@example.com", "encodedPassword123", 991231, 1);
        Member savedMember = memberRepository.save(member);

        // given: 존재하지 않는 Application ID
        Long nonExistentApplicationId = 99999L;

        // given: JWT 토큰 생성
        Map<String, Object> jwtBody = new HashMap<>();
        jwtBody.put("memberId", savedMember.getId());
        String jwt = Ut.Jwt.toString(JWT_SECRET_KEY, JWT_EXPIRE_SECONDS, jwtBody);

        // when & then: PUT 요청 보내고 404 응답 확인
        mockMvc.perform(put("/api/v1/member/policy-aply/welfare-application/" + nonExistentApplicationId)
                        .header("Authorization", "Bearer " + jwt)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value(404))
                .andExpect(jsonPath("$.message").value("신청 내역을 찾지 못했습니다."));
    }

    @Test
    @DisplayName("신청내역 삭제 실패 - 다른 사용자의 신청 내역 삭제 시도 401 + DeleteApplicationResponseDto 반환")
    void deleteApplicationUnauthorizedTest() throws Exception {
        // given: Member 2명 생성 및 저장
        Member member1 = Member.createEmailUser("홍길동", "test1@example.com", "encodedPassword123", 991231, 1);
        Member savedMember1 = memberRepository.save(member1);

        Member member2 = Member.createEmailUser("김철수", "test2@example.com", "encodedPassword123", 990101, 1);
        Member savedMember2 = memberRepository.save(member2);

        // given: Policy 생성 및 저장
        Policy policy = createTestPolicy("TEST-POLICY-001", "테스트 정책");
        Policy savedPolicy = policyRepository.save(policy);

        // given: member1의 Application 생성 및 저장
        Application application = new Application();
        application.setPolicy(savedPolicy);
        application.setApplicant(savedMember1);
        Application savedApplication = applicationRepository.save(application);

        // given: member2의 JWT 토큰 생성 (다른 사용자)
        Map<String, Object> jwtBody = new HashMap<>();
        jwtBody.put("memberId", savedMember2.getId());
        String jwt = Ut.Jwt.toString(JWT_SECRET_KEY, JWT_EXPIRE_SECONDS, jwtBody);

        // when & then: PUT 요청 보내고 401 응답 확인
        mockMvc.perform(put("/api/v1/member/policy-aply/welfare-application/" + savedApplication.getId())
                        .header("Authorization", "Bearer " + jwt)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value(401))
                .andExpect(jsonPath("$.message").value("삭제 권한이 없습니다."));
    }

    // Helper 메서드: Policy 생성
    private Policy createTestPolicy(String plcyNo, String plcyNm) {
        Policy policy = new Policy();
        try {
            Field plcyNoField = Policy.class.getDeclaredField("plcyNo");
            plcyNoField.setAccessible(true);
            plcyNoField.set(policy, plcyNo);

            Field plcyNmField = Policy.class.getDeclaredField("plcyNm");
            plcyNmField.setAccessible(true);
            plcyNmField.set(policy, plcyNm);
        } catch (Exception e) {
            throw new RuntimeException("Policy 필드 설정 실패", e);
        }
        return policy;
    }
}
