package com.back.domain.welfare.policy.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import com.back.domain.welfare.policy.config.YouthPolicyProperties;
import com.back.domain.welfare.policy.dto.PolicyFetchRequestDto;
import com.back.domain.welfare.policy.dto.PolicyFetchResponseDto;
import com.back.domain.welfare.policy.entity.Policy;
import com.back.domain.welfare.policy.repository.PolicyRepository;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PolicyFetchService {

    private final YouthPolicyProperties properties;
    private final PolicyRepository policyRepository;
    private final ObjectMapper objectMapper;

    private final WebClient webClient = WebClient.builder().build();

    private String fetchPolicyFromApi(PolicyFetchRequestDto requestDto) {

        String requestUrl = properties.url()
                + "?apiKeyNm=" + properties.key()
                + "&pageType=" + requestDto.pageType()
                + "&pageSize=" + requestDto.pageSize()
                + "&rtnType=" + requestDto.rtnType();

        return webClient
                .get()
                .uri(requestUrl)
                .retrieve()
                .bodyToMono(String.class)
                .block();
    }

    @Transactional
    public void fetchAndSavePolicies(PolicyFetchRequestDto requestDto) {

        String response = fetchPolicyFromApi(requestDto);

        List<PolicyFetchResponseDto.PolicyItem> items = parsePolicyItems(response);

        List<Policy> policies =
                items.stream().filter(this::isNewPolicy).map(this::toEntity).toList();

        policyRepository.saveAll(policies);
    }

    private List<PolicyFetchResponseDto.PolicyItem> parsePolicyItems(String response) {

        try {
            PolicyFetchResponseDto fetchResponse = objectMapper.readValue(response, PolicyFetchResponseDto.class);

            return fetchResponse.result().youthPolicyList();

        } catch (Exception e) {
            throw new RuntimeException("정책 API 파싱 실패", e);
        }
    }

    private boolean isNewPolicy(PolicyFetchResponseDto.PolicyItem item) {
        return !policyRepository.existsByPlcyNo(item.plcyNo());
    }

    private Policy toEntity(PolicyFetchResponseDto.PolicyItem item) {

        return Policy.builder()
                .plcyNo(item.plcyNo())
                .plcyNm(item.plcyNm())
                .plcyKywdNm(item.plcyKywdNm())
                .plcyExplnCn(item.plcyExplnCn())
                .plcySprtCn(item.plcySprtCn())
                .sprvsnInstCdNm(item.sprvsnInstCdNm())
                .operInstCdNm(item.operInstCdNm())
                .aplyPrdSeCd(item.aplyPrdSeCd())
                .bizPrdBgngYmd(item.bizPrdBgngYmd())
                .bizPrdEndYmd(item.bizPrdEndYmd())
                .plcyAplyMthdCn(item.plcyAplyMthdCn())
                .aplyUrlAddr(item.aplyUrlAddr())
                .sbmsnDcmntCn(item.sbmsnDcmntCn())
                .sprtTrgtMinAge(item.sprtTrgtMinAge())
                .sprtTrgtMaxAge(item.sprtTrgtMaxAge())
                .sprtTrgtAgeLmtYn(item.sprtTrgtAgeLmtYn())
                .mrgSttsCd(item.mrgSttsCd())
                .earnCndSeCd(item.earnCndSeCd())
                .earnMinAmt(item.earnMinAmt())
                .earnMaxAmt(item.earnMaxAmt())
                .zipCd(item.zipCd())
                .jobCd(item.jobCd())
                .schoolCd(item.schoolCd())
                .aplyYmd(item.aplyYmd())
                .sBizCd(item.sbizCd())
                .rawJson(toRawJson(item))
                .build();
    }

    private String toRawJson(Object item) {
        try {
            return objectMapper.writeValueAsString(item);
        } catch (Exception e) {
            throw new RuntimeException("rawJson 변환 실패", e);
        }
    }
}
