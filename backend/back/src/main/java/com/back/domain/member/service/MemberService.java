package com.back.domain.member.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.back.domain.member.dto.JoinRequest;
import com.back.domain.member.dto.JoinResponse;
import com.back.domain.member.entity.Member;
import com.back.domain.member.repository.MemberRepository;
import com.back.global.exception.ServiceException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class MemberService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    public JoinResponse join(JoinRequest req) {

        // 요청값 검증
        if (req == null) {
            throw new ServiceException("MEMBER_400", "요청 바디가 비어 있습니다");
        }
        if (req.email() == null || req.email().isBlank()) {
            throw new ServiceException("MEMBER_400", "이메일은 필수 입력값입니다");
        }
        if (req.password() == null || req.password().isBlank()) {
            throw new ServiceException("MEMBER_400", "비밀번호는 필수 입력값입니다");
        }
        if (req.name() == null || req.name().isBlank()) {
            throw new ServiceException("MEMBER_400", "이름은 필수 입력값입니다");
        }

        // 이메일 중복 체크
        if (memberRepository.existsByEmail(req.email())) {
            throw new ServiceException("MEMBER_409", "이미 존재하는 이메일입니다");
        }

        // 3비밀번호 암호화
        String encodedPassword = passwordEncoder.encode(req.password());

        // 회원 생성 (엔티티 팩토리 메서드 사용)
        Member member =
                Member.createEmailUser(req.name(), req.email(), encodedPassword, req.rrnFront(), req.rrnBackFirst());

        // 저장
        Member savedMember = memberRepository.save(member);

        // 응답 DTO 변환
        return JoinResponse.from(savedMember);
    }
}
