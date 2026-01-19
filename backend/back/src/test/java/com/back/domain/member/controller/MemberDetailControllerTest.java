package com.back.domain.member.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import com.back.domain.member.dto.MemberDetailReq;
import com.back.domain.member.entity.Member;
import com.back.domain.member.entity.MemberDetail;
import com.back.domain.member.entity.MemberDetail.EducationLevel;
import com.back.domain.member.entity.MemberDetail.EmploymentStatus;
import com.back.domain.member.entity.MemberDetail.MarriageStatus;
import com.back.domain.member.repository.MemberDetailRepository;
import com.back.domain.member.repository.MemberRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

@ActiveProfiles("test")
@SpringBootTest
@org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
@Transactional
public class MemberDetailControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private MemberDetailRepository memberDetailRepository;

    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    private Long savedMemberId;

    @BeforeEach
    void setUp() {
        Member member = Member.createEmailUser("김예성", "test@email.com", "encoded_password", 990101, 1);
        Member savedMember = memberRepository.save(member);
        savedMemberId = savedMember.getId();

        MemberDetail detail = MemberDetail.builder()
                .member(savedMember)
                .regionCode("12345")
                .marriageStatus(MarriageStatus.SINGLE)
                .income(3000)
                .employmentStatus(EmploymentStatus.EMPLOYED)
                .educationLevel(EducationLevel.UNIVERSITY)
                .specialStatus("특이사항 없음")
                .build();
        memberDetailRepository.save(detail);
    }

    @Test
    @DisplayName("회원 상세 정보 조회")
    void t1() throws Exception {
        // when
        ResultActions resultActions =
                mockMvc.perform(get("/api/v1/member/detail/" + savedMemberId)).andDo(print());

        resultActions
                .andExpect(status().isOk())
                .andExpect(handler().handlerType(MemberDetailController.class))
                .andExpect(handler().methodName("getMemberDetail"))
                .andExpect(jsonPath("$.name").value("김예성"))
                .andExpect(jsonPath("$.income").value(3000))
                .andExpect(jsonPath("$.regionCode").value("12345"));
    }

    @Test
    @DisplayName("회원 상세 정보 수정")
    void t2() throws Exception {

        MemberDetailReq request = new MemberDetailReq(
                "54321", MarriageStatus.MARRIED, 5000, EmploymentStatus.EMPLOYED, EducationLevel.GRADUATE, "수정된 특이사항");

        ResultActions resultActions = mockMvc.perform(put("/api/v1/member/detail/" + savedMemberId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print());

        resultActions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.regionCode").value("54321"))
                .andExpect(jsonPath("$.income").value(5000))
                .andExpect(jsonPath("$.marriageStatus").value("MARRIED"));
    }
}
