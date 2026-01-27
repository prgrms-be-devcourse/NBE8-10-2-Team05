package com.back.domain.member.member.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.back.domain.member.geo.entity.AddressDto;
import com.back.domain.member.geo.service.GeoService;
import com.back.domain.member.member.dto.MemberDetailReq;
import com.back.domain.member.member.entity.Member;
import com.back.domain.member.member.repository.MemberRepository;
import com.back.global.enumtype.EducationLevel;
import com.back.global.enumtype.EmploymentStatus;
import com.back.global.enumtype.MarriageStatus;
import com.back.global.security.SecurityUser;
import com.back.global.security.jwt.JwtProvider;
import com.fasterxml.jackson.databind.ObjectMapper;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class MemberControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtProvider jwtProvider;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private GeoService geoService;

    @Test
    @DisplayName("로그인 성공 - 200 반환 + memberId/name + accessToken 반환")
    void login_success() throws Exception {
        // given: 회원 가입
        String joinBody = """
        {
          "name": "홍길동",
          "email": "login_test@example.com",
          "password": "12345678",
          "rrnFront": 991231,
          "rrnBackFirst": 1
        }
        """;

        mvc.perform(post("/api/v1/member/member/join")
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
        mvc.perform(post("/api/v1/member/member/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginBody))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))

                // 로그인 응답 기본 필드
                .andExpect(jsonPath("$.memberId").exists())
                .andExpect(jsonPath("$.name").value("홍길동"))

                // JWT Access Token 검증
                .andExpect(jsonPath("$.accessToken").exists())
                .andExpect(jsonPath("$.accessToken").isString())
                .andExpect(jsonPath("$.accessToken").isNotEmpty());
    }

    @Test
    @DisplayName("로그인 실패 - 비밀번호 불일치면 401 반환")
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

        mvc.perform(post("/api/v1/member/member/join")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(joinBody))
                .andExpect(status().isOk());

        String loginBody = """
        {
          "email": "wrong_pw@example.com",
          "password": "WRONG_PASSWORD"
        }
        """;

        mvc.perform(post("/api/v1/member/member/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginBody))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.resultCode").value("AUTH-401"));
    }

    @Test
    @DisplayName("로그인 실패 - 존재하지 않는 이메일이면 404 반환")
    void login_fail_email_not_found() throws Exception {
        // given
        String loginBody = """
        {
          "email": "notfound@example.com",
          "password": "12345678"
        }
        """;

        mvc.perform(post("/api/v1/member/member/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginBody))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.resultCode").value("MEMBER-404"));
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
        mvc.perform(post("/api/v1/member/member/join")
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

        mvc.perform(post("/api/v1/member/member/join")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk());

        // when: 같은 이메일로 2번 가입
        var result = mvc.perform(post("/api/v1/member/member/join")
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

        mvc.perform(post("/api/v1/member/member/join")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("me 실패 - 토큰 없으면 401")
    void me_fail_without_token() throws Exception {
        mvc.perform(get("/api/v1/member/member/me")).andExpect(status().isUnauthorized()); // 401
    }

    @Test
    @DisplayName("me 성공 - 토큰 있으면 200 + 내 정보 반환")
    void me_success_with_token() throws Exception {
        // given: 테스트용 회원 1명 저장(회원가입 API 호출 대신 repo로 직접 저장)
        Member member =
                Member.createEmailUser("홍길동", "me_test@example.com", passwordEncoder.encode("12345678"), 991231, 1);
        Member saved = memberRepository.save(member);

        //        // 토큰 발급
        //        String accessToken = jwtProvider.issueAccessToken(
        //                saved.getId(), saved.getEmail(), saved.getRole().name());

        // test에서는 JWT를 안 타므로, SecurityContext에 직접 인증 주입
        var auth = new UsernamePasswordAuthenticationToken(
                saved.getId(), // principal을 memberId(Long)로 넣기 (서비스가 이걸 꺼냄)
                null,
                List.of(new SimpleGrantedAuthority("ROLE_" + saved.getRole().name())));
        SecurityContextHolder.getContext().setAuthentication(auth);

        // when & then
        mvc.perform(get("/api/v1/member/member/me")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + null)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(saved.getId()))
                .andExpect(jsonPath("$.name").value("홍길동"))
                .andExpect(jsonPath("$.email").value("me_test@example.com"));
    }

    @Test
    @DisplayName("내 상세 정보 조회")
    void getDetail() throws Exception {
        // given: 테스트용 회원 1명 저장(회원가입 API 호출 대신 repo로 직접 저장)
        Member member =
                Member.createEmailUser("홍길동", "me_test@example.com", passwordEncoder.encode("12345678"), 991231, 1);
        Member saved = memberRepository.save(member);

        SecurityUser testUser =
                new SecurityUser(saved.getId().intValue(), saved.getEmail(), "", saved.getName(), java.util.List.of());

        mvc.perform(get("/api/v1/member/member/detail").with(user(testUser))) // 인증된 사용자 주입
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("홍길동"))
                .andExpect(jsonPath("$.email").value("me_test@example.com"));
    }

    @Test
    @DisplayName("내 상세 정보 수정")
    void modifyDetail() throws Exception {
        // 1. Given: 회원 생성 및 시큐리티 유저 준비
        Member member =
                Member.createEmailUser("홍길동", "me_test@example.com", passwordEncoder.encode("12345678"), 991231, 1);
        Member saved = memberRepository.save(member);
        SecurityUser testUser =
                new SecurityUser(saved.getId().intValue(), saved.getEmail(), "", saved.getName(), java.util.List.of());

        // 2. 수정 데이터 준비 (DTO 패키지 경로 및 생성자 확인 필요)
        MemberDetailReq request = new MemberDetailReq(
                "54321",
                MarriageStatus.MARRIED,
                5000,
                EmploymentStatus.EMPLOYED,
                EducationLevel.UNIVERSITY_GRADUATED,
                "수정된 특이사항");

        // 3. When & Then: PUT 요청 수행 및 결과 검증
        mvc.perform(put("/api/v1/member/member/detail")
                        .with(user(testUser)) // 인증 주입
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))) // JSON 변환
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.regionCode").value("54321"))
                .andExpect(jsonPath("$.income").value(5000));
    }

    @Test
    @DisplayName("상세 정보 없는 멤버 정보 조회 -> MemberDetail 자동 생성 & null 값인지 확인")
    void autoCreateDetail() throws Exception {
        // 1. Given: 상세 정보가 없는 신규 회원 생성
        Member newMember = Member.createEmailUser("신규회원", "new@email.com", passwordEncoder.encode("pass"), 000101, 1);
        Member savedNewMember = memberRepository.save(newMember);

        // 2. 신규 회원을 위한 SecurityUser 생성
        SecurityUser newUser = new SecurityUser(
                savedNewMember.getId().intValue(),
                savedNewMember.getEmail(),
                "",
                savedNewMember.getName(),
                java.util.List.of());

        // 3. When & Then: GET 요청 수행 (URL에서 ID 제거 및 인증 주입)
        mvc.perform(get("/api/v1/member/member/detail").with(user(newUser))) // 신규 유저로 인증
                .andDo(print())
                .andExpect(status().isOk())
                // Member 기본 정보 확인
                .andExpect(jsonPath("$.name").value("신규회원"))
                .andExpect(jsonPath("$.email").value("new@email.com"))
                // 상세 정보 필드들이 최초 생성 시 null인지 확인 (org.hamcrest.Matchers.nullValue() 사용)
                .andExpect(jsonPath("$.regionCode").value(org.hamcrest.Matchers.nullValue()))
                .andExpect(jsonPath("$.income").value(org.hamcrest.Matchers.nullValue()))
                .andExpect(jsonPath("$.marriageStatus").value(org.hamcrest.Matchers.nullValue()))
                .andExpect(jsonPath("$.employmentStatus").value(org.hamcrest.Matchers.nullValue()))
                .andExpect(jsonPath("$.educationLevel").value(org.hamcrest.Matchers.nullValue()));
    }

    @Test
    @DisplayName("주소 업데이트 성공 테스트")
    void updateAddress_Success() throws Exception {

        Member member = Member.createEmailUser("테스트", "test@test.com", passwordEncoder.encode("pass"), 991231, 1);
        Member saved = memberRepository.save(member);

        AddressDto requestDto = AddressDto.builder()
                .postcode("12345")
                .addressName("서울특별시 강남구 테헤란로 427")
                .build();

        AddressDto enrichedDto = AddressDto.builder()
                .postcode("12345")
                .addressName("서울특별시 강남구 테헤란로 427")
                .hCode("4514069000")
                .latitude(37.503)
                .longitude(127.044)
                .build();

        given(geoService.getGeoCode(any(AddressDto.class))).willReturn(enrichedDto);

        SecurityUser testUser =
                new SecurityUser(saved.getId().intValue(), saved.getEmail(), "", saved.getName(), java.util.List.of());

        mvc.perform(put("/api/v1/member/member/detail/address")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto))
                        .with(csrf())
                        .with(user(testUser)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.hCode").value("4514069000"))
                .andExpect(jsonPath("$.latitude").value(37.503))
                .andExpect(jsonPath("$.longitude").value(127.044));
    }
}
