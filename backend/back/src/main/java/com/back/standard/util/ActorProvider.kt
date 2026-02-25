package com.back.standard.util

import com.back.domain.member.member.entity.Member
import com.back.domain.member.member.repository.MemberRepository
import com.back.global.exception.ServiceException
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class ActorProvider(private val memberRepository: MemberRepository) {

    @Transactional(readOnly = true)
    fun getActor(): Member {
        val auth = SecurityContextHolder.getContext().authentication

        if (auth == null || !auth.isAuthenticated || auth.principal == null || auth.principal == "anonymousUser") {
            throw ServiceException("AUTH-401", "인증 정보가 없습니다.")
        }

        val memberId = when (val principal = auth.principal) {
            is Long -> principal
            is String -> principal.toLongOrNull()
            else -> null
        } ?: throw ServiceException("AUTH-401", "유효하지 않은 인증 형식입니다.")

        return memberRepository.findById(memberId)
            .orElseThrow { ServiceException("MEMBER-404", "존재하지 않는 회원입니다.") }
        }
}

