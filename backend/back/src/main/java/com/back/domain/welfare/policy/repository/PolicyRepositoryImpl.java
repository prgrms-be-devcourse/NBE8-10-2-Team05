package com.back.domain.welfare.policy.repository;

import static com.back.domain.welfare.policy.entity.QPolicy.policy;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.back.domain.welfare.policy.dto.PolicyRequestDto;
import com.back.domain.welfare.policy.dto.PolicyResponseDto;
import com.back.domain.welfare.policy.dto.QPolicyResponseDto;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class PolicyRepositoryImpl implements PolicyRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<PolicyResponseDto> search(PolicyRequestDto condition) {

        BooleanBuilder builder = new BooleanBuilder();

        // 나이 조건
        if (condition.getSprtTrgtMinAge() != null) {
            builder.and(policy.sprtTrgtMinAge.goe(String.valueOf(condition.getSprtTrgtMinAge())));
        }
        if (condition.getSprtTrgtMaxAge() != null) {
            builder.and(policy.sprtTrgtMaxAge.loe(String.valueOf(condition.getSprtTrgtMaxAge())));
        }

        // 우편번호, 학교, 직업 조건
        if (condition.getZipCd() != null) {
            builder.and(policy.zipCd.eq(condition.getZipCd()));
        }
        if (condition.getSchoolCd() != null) {
            builder.and(policy.schoolCd.eq(condition.getSchoolCd()));
        }
        if (condition.getJobCd() != null) {
            builder.and(policy.jobCd.eq(condition.getJobCd()));
        }

        // 소득 조건
        if (condition.getEarnMinAmt() != null) {
            builder.and(policy.earnMinAmt.goe(String.valueOf(condition.getEarnMinAmt())));
        }
        if (condition.getEarnMaxAmt() != null) {
            builder.and(policy.earnMaxAmt.loe(String.valueOf(condition.getEarnMaxAmt())));
        }

        // Projection: PolicyResponseDto record에 맞게 select
        return queryFactory
                .select(new QPolicyResponseDto(
                        policy.id,
                        policy.plcyNo,
                        policy.plcyNm,
                        policy.plcyExplnCn,
                        policy.plcySprtCn,
                        policy.plcyKywdNm,
                        policy.sprtTrgtMinAge,
                        policy.sprtTrgtMaxAge,
                        policy.zipCd,
                        policy.schoolCd,
                        policy.jobCd,
                        policy.earnMinAmt,
                        policy.earnMaxAmt,
                        policy.aplyYmd,
                        policy.aplyUrlAddr,
                        policy.aplyMthdCn,
                        policy.sbmsnDcmntCn,
                        policy.operInstCdNm))
                .from(policy)
                .where(builder)
                .fetch();
    }
}
