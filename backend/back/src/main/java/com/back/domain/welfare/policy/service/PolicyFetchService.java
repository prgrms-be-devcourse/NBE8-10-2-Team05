package com.back.domain.welfare.policy.service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.back.domain.welfare.policy.dto.PolicyFetchRequestDto;
import com.back.domain.welfare.policy.dto.PolicyFetchResponseDto;
import com.back.domain.welfare.policy.dto.PolicyFetchResponseDto.PolicyItem;
import com.back.domain.welfare.policy.entity.Policy;
import com.back.domain.welfare.policy.repository.PolicyRepository;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class PolicyFetchService {

    private final PolicyRepository policyRepository;
    private final PolicyApiClient policyApiClient;
    private final ObjectMapper objectMapper;

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
                Thread.sleep(500); // API 과부하 방지
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            PolicyFetchResponseDto nextFetchResponse = policyApiClient.fetchPolicyPage(requestDto, pageNum, pageSize);
            log.info(
                    "Fetched plcyNos: {}",
                    nextFetchResponse.result().youthPolicyList().stream()
                            .map(PolicyItem::plcyNo)
                            .toList());

            log.info(
                    "Page {}: items={}",
                    pageNum,
                    nextFetchResponse.result().youthPolicyList().size());
            savePolicies(nextFetchResponse.result().youthPolicyList());
        }
    }

    private void savePolicies(List<PolicyFetchResponseDto.PolicyItem> items) {
        Set<String> pagePlcyNos = new HashSet<>();
        // HashSet으로 메모리 중복 체크
        List<Policy> policies = items.stream()
                .filter(item -> !pagePlcyNos.contains(item.plcyNo()))
                .map(this::toEntity)
                .toList();

        // HashSet에 저장 전 현재 상태 확인
        log.info(
                "savedPlcyNos size={} preview={}",
                pagePlcyNos.size(),
                pagePlcyNos.stream().limit(10).toList());
        // 저장 전 로그 확인
        log.info(
                "Saving {} policies: {}",
                policies.size(),
                policies.stream().map(Policy::getPlcyNo).toList());

        // HashSet에 저장
        policies.forEach(p -> pagePlcyNos.add(p.getPlcyNo()));

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
            throw new RuntimeException("Entity 변환 실패", e);
        }
    }
}
