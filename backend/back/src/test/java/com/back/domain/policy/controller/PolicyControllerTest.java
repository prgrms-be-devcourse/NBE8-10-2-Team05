package com.back.domain.policy.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.back.domain.welfare.policy.dto.PolicyResponseDto;
import com.back.domain.welfare.policy.service.PolicyService;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class PolicyControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PolicyService policyService;

    @Test
    @DisplayName("복지 정책 검색 API 성공")
    void searchPolicy_success() throws Exception {
        // given
        PolicyResponseDto policy = PolicyResponseDto.builder()
                .id(1L)
                .plcyNo("2025-YS-001")
                .plcyNm("청년 주거 지원 정책")
                .plcyExplnCn("청년의 주거 안정을 위해 월세를 지원하는 정책입니다.")
                .plcySprtCn("월 최대 20만원, 최대 12개월 지원")
                .plcyKywdNm("청년,주거,월세지원")
                .sprtTrgtMinAge("19")
                .sprtTrgtMaxAge("34")
                .zipCd("11")
                .schoolCd("01")
                .jobCd("02")
                .earnMinAmt("0")
                .earnMaxAmt("3500000")
                .aplyYmd("2025-01-01 ~ 2025-12-31")
                .aplyUrlAddr("https://www.gov.kr/portal/service/serviceInfo/123456")
                .plcyAplyMthdCn("온라인 신청")
                .sbmsnDcmntCn("신청서, 주민등록등본, 소득증빙서류")
                .operInstCdNm("보건복지부")
                .build();

        given(policyService.search(any())).willReturn(List.of(policy));

        // when & then
        mockMvc.perform(get("/api/v1/welfare/policy/search")
                        .param("sprtTrgtMinAge", "19")
                        .param("sprtTrgtMaxAge", "34")
                        .param("zipCd", "11")
                        .param("schoolCd", "01")
                        .param("jobCd", "02")
                        .param("earnMinAmt", "0")
                        .param("earnMaxAmt", "3500000"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.welfares").isArray())
                .andExpect(jsonPath("$.welfares[0].id").value(1))
                .andExpect(jsonPath("$.welfares[0].plcyNo").value("2025-YS-001"))
                .andExpect(jsonPath("$.welfares[0].plcyNm").value("청년 주거 지원 정책"))
                .andExpect(jsonPath("$.welfares[0].sprtTrgtMinAge").value("19"))
                .andExpect(jsonPath("$.welfares[0].sprtTrgtMaxAge").value("34"))
                .andExpect(jsonPath("$.welfares[0].earnMaxAmt").value("3500000"));
    }
}
