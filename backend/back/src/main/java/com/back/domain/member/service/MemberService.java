package com.back.domain.member.service;

import java.time.Duration;
import java.time.LocalDateTime;

import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.back.domain.auth.entity.RefreshToken;
import com.back.domain.auth.repository.RefreshTokenRepository;
import com.back.domain.auth.util.RefreshTokenGenerator;
import com.back.domain.auth.util.TokenHasher;
import com.back.domain.member.dto.*;
import com.back.domain.member.entity.Member;
import com.back.domain.member.repository.MemberRepository;
import com.back.global.exception.ServiceException;
import com.back.global.security.jwt.JwtProvider;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class MemberService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;

    // refresh 저장소
    private final RefreshTokenRepository refreshTokenRepository;

    // refresh 토큰 만료 기간 14일로 가정함
    private static final int REFRESH_DAYS = 14;

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

    @Transactional
    public LoginResponse login(LoginRequest req, HttpServletResponse response) {
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

        // AccesToken (JWT)발급
        String token =
                jwtProvider.issueAccessToken(member.getId(), member.getEmail(), String.valueOf(member.getRole()));

        // RefreshToken(UUID) 생성 + DB 저장 (추가)
        // 2-1) 클라이언트에게 줄 refresh token "원문" 생성 (UUID)
        String rawRefreshToken = RefreshTokenGenerator.generate();

        // 2-2) DB 저장용 hash 생성 (보안상 원문 저장 X)
        String refreshTokenHash = TokenHasher.sha256Hex(rawRefreshToken);

        // 2-3) 만료 시각 계산 (ex: 14일 뒤)
        LocalDateTime expiresAt = LocalDateTime.now().plusDays(REFRESH_DAYS);

        // 2-4) 엔티티 생성 후 DB 저장
        RefreshToken refreshToken = RefreshToken.create(member, refreshTokenHash, expiresAt);
        refreshTokenRepository.save(refreshToken);

        // Access Token을 HttpOnly 쿠키로 내려준다
        // 브라우저가 자동으로 저장하고 이후 요청에 자동 포함
        ResponseCookie cookie = ResponseCookie.from("accessToken", token)
                .httpOnly(true) // JS에서 접근 불가 (XSS 방어)
                .secure(false) // dev 환경(http) → false / prod(https) → true
                .path("/") // 모든 경로에서 쿠키 전송
                .sameSite("Lax") // 로컬 개발에서 가장 무난
                .maxAge(Duration.ofMinutes(20)) // Access Token 유효시간
                .build();

        // Set-Cookie 헤더에 추가
        response.addHeader("Set-Cookie", cookie.toString());
        response.addHeader("Set-Cookie", buildRefreshCookieHeader(rawRefreshToken));

        return new LoginResponse(member.getId(), member.getName(), token);
    }

    @Transactional(readOnly = true)
    public MeResponse me() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null
                || !auth.isAuthenticated()
                || auth.getPrincipal() == null
                || "anonymousUser".equals(auth.getPrincipal())) {
            throw new ServiceException("AUTH-401", "인증 정보가 없습니다.");
        }

        // get actor같은 연할을 하는 곳인데 강사님은 DB조회를 안하고 나는 DB조희를 함
        Long memberId;
        try {
            // principal에 memberId를 넣어둔 상태라서 이렇게 꺼내면 됨
            memberId = (Long) auth.getPrincipal();
        } catch (ClassCastException e) {
            // 혹시 String으로 들어오는 경우 대비
            memberId = Long.valueOf(String.valueOf(auth.getPrincipal()));
        }

        Member member = memberRepository
                .findById(memberId)
                .orElseThrow(() -> new ServiceException("MEMBER-404", "존재하지 않는 회원입니다."));

        return new MeResponse(member.getId(), member.getName(), member.getEmail());
    }

    // accessToken 쿠키를 만료(Max-Age=0)시켜 브라우저에서 제거한다.
    public String buildLogoutSetCookieHeader() {
        return ResponseCookie.from("accessToken", "")
                .httpOnly(true)
                .secure(false) // dev(http) 기준. prod(https)면 true로 분기 필요
                .path("/")
                .sameSite("Lax")
                .maxAge(Duration.ZERO) // Max-Age=0
                .build()
                .toString();
    }

    // 리프레시 토큰은 길게 가져감 -> 14일
    private String buildRefreshCookieHeader(String rawRefreshToken) {
        return ResponseCookie.from("refreshToken", rawRefreshToken)
                .httpOnly(true)
                .secure(false) // dev
                .path("/")
                .sameSite("Lax")
                .maxAge(Duration.ofDays(REFRESH_DAYS))
                .build()
                .toString();
    }
}
