package com.back.domain.policy.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.back.domain.welfare.policy.dto.PolicyRequestDto;
import com.back.domain.welfare.policy.dto.PolicyResponseDto;
import com.back.domain.welfare.policy.repository.PolicyRepositoryCustom;
import com.back.domain.welfare.policy.service.PolicyService;

public class PolicyServiceTest {

    @Mock
    private PolicyRepositoryCustom policyRepository;

    @InjectMocks
    private PolicyService policyService;

    private PolicyRequestDto requestDto;
    private PolicyResponseDto responseDto;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // 테스트용 PolicyRequestDto
        requestDto = PolicyRequestDto.builder()
                .sprtTrgtMinAge(20)
                .sprtTrgtMaxAge(30)
                .zipCd("12345")
                .schoolCd("SCH001")
                .jobCd("JOB001")
                .earnMinAmt(2000)
                .earnMaxAmt(5000)
                .build();

        // 테스트용 PolicyResponseDto
        responseDto = new PolicyResponseDto(
                1L,
                "PLCY001",
                "Test Policy",
                "설명",
                "지원 내용",
                "키워드",
                "20",
                "30",
                "12345",
                "SCH001",
                "JOB001",
                "2000",
                "5000",
                "20260101",
                "http://apply.url",
                "온라인",
                "서류",
                "운영기관");
    }

    @Test
    void search_ReturnsListOfPolicyResponseDto() {
        // given
        when(policyRepository.search(requestDto)).thenReturn(List.of(responseDto));

        // when
        List<PolicyResponseDto> result = policyService.search(requestDto);

        // then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).plcyNm()).isEqualTo("Test Policy");

        // repository의 search 메서드가 정확히 1번 호출되었는지 검증
        verify(policyRepository, times(1)).search(requestDto);
    }
}
