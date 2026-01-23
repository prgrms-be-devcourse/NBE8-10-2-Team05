package com.back.domain.welfare.policy.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.back.domain.welfare.policy.dto.PolicyFetchRequestDto;
import com.back.domain.welfare.policy.dto.PolicyFetchResponseDto;
import com.back.domain.welfare.policy.dto.PolicyFetchResponseDto.Pagging;
import com.back.domain.welfare.policy.dto.PolicyFetchResponseDto.PolicyItem;
import com.back.domain.welfare.policy.dto.PolicyFetchResponseDto.Result;
import com.back.domain.welfare.policy.entity.Policy;
import com.back.domain.welfare.policy.repository.PolicyRepository;
import com.fasterxml.jackson.databind.ObjectMapper;

public class PolicyFetchServiceTest {

    @Mock
    private PolicyRepository policyRepository;

    @Mock
    private PolicyApiClient policyApiClient;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private PolicyFetchService policyFetchService;

    private PolicyFetchRequestDto requestDto;
    private PolicyFetchResponseDto responseDto;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);

        requestDto = new PolicyFetchRequestDto("API_KEY", "1", "100", "json");

        PolicyItem item1 = new PolicyItem(
                "PLCY001",
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                "정책1",
                "키워드",
                "설명",
                null,
                null,
                "지원내용",
                null,
                "주관기관",
                null,
                null,
                "운영기관",
                null,
                null,
                "001",
                "002",
                "20240101",
                "20241231",
                null,
                "온라인",
                null,
                "http://apply",
                "서류",
                null,
                null,
                null,
                null,
                null,
                "20",
                "30",
                "N",
                "001",
                "002",
                "0",
                "5000",
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                "12345",
                null,
                "null",
                "null",
                "JOB001",
                "SCH001",
                "20240101",
                null,
                null,
                "SBIZ001");

        PolicyItem item2 = new PolicyItem(
                "PLCY002",
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                "정책2",
                "키워드",
                "설명",
                null,
                null,
                "지원내용",
                null,
                "주관기관",
                null,
                null,
                "운영기관",
                null,
                null,
                "001",
                "002",
                "20240101",
                "20241231",
                null,
                "온라인",
                null,
                "http://apply",
                "서류",
                null,
                null,
                null,
                null,
                null,
                "20",
                "30",
                "N",
                "001",
                "002",
                "0",
                "5000",
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                "12345",
                null,
                "null",
                "null",
                "JOB001",
                "SCH001",
                "20240101",
                null,
                null,
                "SBIZ002");

        Pagging pagging = new Pagging(2, 1, 100);
        Result result = new Result(pagging, List.of(item1, item2));

        responseDto = new PolicyFetchResponseDto(0, "SUCCESS", result);

        when(objectMapper.writeValueAsString(any())).thenReturn("{json}");
    }

    @Test
    void fetchAndSavePolicies_savesOnlyNewPolicies() {

        // given
        when(policyApiClient.fetchPolicyPage(any(), eq(1), eq(100))).thenReturn(responseDto);

        // PLCY001 은 이미 DB에 존재
        when(policyRepository.findExistingPlcyNos(Set.of("PLCY001", "PLCY002"))).thenReturn(Set.of("PLCY001"));

        // when
        policyFetchService.fetchAndSavePolicies(requestDto);

        // then
        verify(policyRepository, times(1)).saveAll(argThat(policies -> {
            assertThat(policies).hasSize(1);

            Policy policy = policies.iterator().next();
            assertThat(policy.getPlcyNo()).isEqualTo("PLCY002");

            return true;
        }));
    }
}
