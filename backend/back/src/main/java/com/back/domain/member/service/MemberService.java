package com.back.domain.member.service;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.back.domain.member.dto.*;
import com.back.domain.member.entity.Member;
import com.back.domain.member.repository.MemberRepository;
import com.back.global.exception.ServiceException;
import com.back.global.security.jwt.JwtProvider;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class MemberService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;

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

        // 비밀번호 암호화
        String encodedPassword = passwordEncoder.encode(req.password());

        // 회원 생성 (엔티티 팩토리 메서드 사용)
        Member member =
                Member.createEmailUser(req.name(), req.email(), encodedPassword, req.rrnFront(), req.rrnBackFirst());

        Member savedMember = memberRepository.save(member);

        return JoinResponse.from(savedMember);
    }

    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest req) {
        if (req.getEmail() == null || req.getEmail().isBlank()) {
            throw new ServiceException("AUTH-400", "email은 필수입니다.");
        }
        if (req.getPassword() == null || req.getPassword().isBlank()) {
            throw new ServiceException("AUTH-400", "password는 필수입니다.");
        }

        Member member = memberRepository
                .findByEmail(req.getEmail())
                .orElseThrow(() -> new ServiceException("MEMBER-404", "존재하지 않는 이메일입니다."));

        if (member.getType() != null && member.getType() != Member.LoginType.EMAIL) {
            throw new ServiceException("AUTH-400", "소셜 로그인 계정입니다. 소셜 로그인을 이용해주세요.");
        }

        if (!passwordEncoder.matches(req.getPassword(), member.getPassword())) {
            throw new ServiceException("AUTH-401", "비밀번호가 일치하지 않습니다.");
        }

        String token =
                jwtProvider.issueAccessToken(member.getId(), member.getEmail(), String.valueOf(member.getRole()));

        return new LoginResponse(member.getId(), member.getName(), token);
    }

    @Transactional(readOnly = true)
    public MeResponse me() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || auth.getName() == null) {
            throw new ServiceException("AUTH-401", "인증 정보가 없습니다.");
        }

        // 우리가 JWT subject에 email 넣는다고 가정 (보통 이 방식)
        String email = auth.getName();

        Member member = memberRepository
                .findByEmail(email)
                .orElseThrow(() -> new ServiceException("MEMBER-404", "존재하지 않는 회원입니다."));

        return new MeResponse(member.getId(), member.getName(), member.getEmail());
    }
}
