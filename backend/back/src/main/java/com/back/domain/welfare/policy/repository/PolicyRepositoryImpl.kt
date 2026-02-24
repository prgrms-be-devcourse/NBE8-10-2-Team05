package com.back.domain.welfare.policy.repository

import com.back.domain.welfare.policy.dto.PolicySearchRequestDto
import com.back.domain.welfare.policy.dto.PolicySearchResponseDto
import com.back.domain.welfare.policy.dto.QPolicySearchResponseDto
import com.back.domain.welfare.policy.entity.QPolicy.policy
import com.querydsl.core.BooleanBuilder
import com.querydsl.jpa.impl.JPAQueryFactory
import org.springframework.stereotype.Repository

@Repository
class PolicyRepositoryImpl(
    private val queryFactory: JPAQueryFactory
) : PolicyRepositoryCustom {

    override fun search(condition: PolicySearchRequestDto): List<PolicySearchResponseDto> {

        val builder = BooleanBuilder().apply {

            condition.sprtTrgtMinAge?.let {
                and(policy.sprtTrgtMinAge.goe(it.toString()))
            }

            condition.sprtTrgtMaxAge?.let {
                and(policy.sprtTrgtMaxAge.loe(it.toString()))
            }

            condition.zipCd?.let {
                and(policy.zipCd.eq(it))
            }

            condition.schoolCd?.let {
                and(policy.schoolCd.eq(it))
            }

            condition.jobCd?.let {
                and(policy.jobCd.eq(it))
            }

            condition.earnMinAmt?.let {
                and(policy.earnMinAmt.goe(it.toString()))
            }

            condition.earnMaxAmt?.let {
                and(policy.earnMaxAmt.loe(it.toString()))
            }
        }

        return queryFactory
            .select(
                QPolicySearchResponseDto(
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
                    policy.plcyAplyMthdCn,
                    policy.sbmsnDcmntCn,
                    policy.operInstCdNm
                )
            )
            .from(policy)
            .where(builder)
            .fetch()
    }
}