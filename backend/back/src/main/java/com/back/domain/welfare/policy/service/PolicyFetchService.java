package com.back.domain.welfare.policy.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.back.domain.welfare.policy.dto.PolicyFetchRequestDto;
import com.back.domain.welfare.policy.dto.PolicyFetchResponseDto;
import com.back.domain.welfare.policy.entity.Policy;
import com.back.domain.welfare.policy.repository.PolicyRepository;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PolicyFetchService {

    private final PolicyRepository policyRepository;
    private final PolicyApiClient policyApiClient;
    private final ObjectMapper objectMapper; // Bean 주입

    @Transactional
    public void fetchAndSavePolicies(PolicyFetchRequestDto requestDto) {

        int pageSize = 100;
        int pageNum = 1;

        // 1페이지 호출
        PolicyFetchResponseDto fetchResponse = policyApiClient.fetchPolicyPage(requestDto, pageNum, pageSize);

        int totalCnt = fetchResponse.result().pagging().totCount();
        int totalPages = (int) Math.ceil((double) totalCnt / pageSize);

        // 1페이지 저장
        savePolicies(fetchResponse.result().youthPolicyList());

        // 2페이지 이상 반복
        for (pageNum = 2; pageNum <= totalPages; pageNum++) {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            PolicyFetchResponseDto nextFetchResponse = policyApiClient.fetchPolicyPage(requestDto, pageNum, pageSize);
            savePolicies(nextFetchResponse.result().youthPolicyList());
        }
    }

    private void savePolicies(List<PolicyFetchResponseDto.PolicyItem> items) {
        List<Policy> policies = items.stream()
                .filter(item -> !policyRepository.existsByPlcyNo(item.plcyNo()))
                .map(this::toEntity)
                .toList();

        policyRepository.saveAll(policies);
    }

    private Policy toEntity(PolicyFetchResponseDto.PolicyItem item) {
        try {
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
                    .rawJson(objectMapper.writeValueAsString(item))
                    .build();
        } catch (Exception e) {
            throw new RuntimeException("Entity변환 실패", e);
        }
    }
}
