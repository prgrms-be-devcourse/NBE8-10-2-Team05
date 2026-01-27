package com.back.domain.member.member.service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;

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
import com.back.domain.member.member.dto.*;
import com.back.domain.member.member.entity.Member;
import com.back.domain.member.member.repository.MemberRepository;
import com.back.global.exception.ServiceException;
import com.back.global.security.jwt.JwtProvider;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
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
    public void completeSocialSignup(CompleteSocialSignupRequest req) {
        if (req == null) throw new ServiceException("MEMBER-400", "요청 바디가 비었습니다.");

        if (req.rrnFront() == null) throw new ServiceException("MEMBER-400", "rrnFront는 필수입니다.");
        if (req.rrnBackFirst() == null) throw new ServiceException("MEMBER-400", "rrnBackFirst는 필수입니다.");

        // 현재 로그인 사용자 조회 (기존 me() 로직 재사용 가능)
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null
                || !auth.isAuthenticated()
                || auth.getPrincipal() == null
                || "anonymousUser".equals(auth.getPrincipal())) {
            throw new ServiceException("AUTH-401", "인증 정보가 없습니다.");
        }

        Long memberId;
        try {
            memberId = (Long) auth.getPrincipal();
        } catch (ClassCastException e) {
            memberId = Long.valueOf(String.valueOf(auth.getPrincipal()));
        }

        Member member = memberRepository
                .findById(memberId)
                .orElseThrow(() -> new ServiceException("MEMBER-404", "존재하지 않는 회원입니다."));

        // 소셜 미완성 유저만 완료 처리
        if (member.getStatus() != Member.MemberStatus.PRE_REGISTERED) {
            throw new ServiceException("MEMBER-400", "이미 가입 완료된 회원입니다.");
        }

        member.completeSocialSignup(req.rrnFront(), req.rrnBackFirst());
    }

    // 로그인 결과(바디 + Set-Cookie 2개)
    public record LoginResult(LoginResponse body, String accessSetCookieHeader, String refreshSetCookieHeader) {}

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

        // 공통 발급 로직 호출 (access + refresh 쿠키 세팅 + DB 저장)
        String accessToken = issueLoginCookies(member, response);

        return new LoginResponse(member.getId(), member.getName(), accessToken);
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

    public record LogoutCookieHeaders(String deleteAccessCookieHeader, String deleteRefreshCookieHeader) {}

    @Transactional
    public LogoutCookieHeaders logout(HttpServletRequest request) {

        // 1) refreshToken 쿠키 원문 읽기
        String rawRefreshToken = getCookieValue(request, "refreshToken");

        // 2) refreshToken이 있으면 DB에서 찾아서 폐기(revoke)
        if (rawRefreshToken != null && !rawRefreshToken.isBlank()) {
            String hash = TokenHasher.sha256Hex(rawRefreshToken);

            refreshTokenRepository.findByTokenHash(hash).ifPresent(rt -> {
                rt.revoke(); // revokedAt = now
                // rt가 영속 상태면 save 없어도 되지만, 안전하게 save 해도 됨
                refreshTokenRepository.save(rt);
            });

            // delete로 하고 싶으면 revoke 대신 이걸로 교체 가능
            // refreshTokenRepository.findByTokenHash(hash).ifPresent(refreshTokenRepository::delete);
        }

        // access/refresh 쿠키 둘 다 삭제 헤더 생성해서 반환
        String deleteAccessCookie = buildDeleteCookieHeader("accessToken");
        String deleteRefreshCookie = buildDeleteCookieHeader("refreshToken");

        return new LogoutCookieHeaders(deleteAccessCookie, deleteRefreshCookie);
    }

    // 요청에서 쿠키값 꺼내기
    private String getCookieValue(HttpServletRequest request, String name) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) return null;

        for (Cookie cookie : cookies) {
            if (name.equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        return null;
    }
    // 쿠키 삭제용 Max-Age=0
    private String buildDeleteCookieHeader(String cookieName) {
        return ResponseCookie.from(cookieName, "")
                .httpOnly(true)
                .secure(false) // dev
                .path("/")
                .sameSite("Lax")
                .maxAge(Duration.ZERO)
                .build()
                .toString();
    }

    @Transactional(readOnly = true)
    public Optional<Member> findById(Long id) {
        return memberRepository.findById(id);
    }

    @Transactional
    public String issueLoginCookies(Member member, HttpServletResponse response) {

        // 1) AccessToken 발급
        String accessToken = jwtProvider.issueAccessToken(
                member.getId(), member.getEmail() == null ? "" : member.getEmail(), String.valueOf(member.getRole()));

        // 2) RefreshToken 생성
        String rawRefreshToken = RefreshTokenGenerator.generate();
        String refreshTokenHash = TokenHasher.sha256Hex(rawRefreshToken);

        LocalDateTime expiresAt = LocalDateTime.now().plusDays(14);

        RefreshToken refreshToken = RefreshToken.create(member, refreshTokenHash, expiresAt);
        refreshTokenRepository.save(refreshToken);

        // 3) AccessToken 쿠키
        ResponseCookie accessCookie = ResponseCookie.from("accessToken", accessToken)
                .httpOnly(true)
                .secure(false) // dev
                .path("/")
                .sameSite("Lax")
                .maxAge(Duration.ofMinutes(20))
                .build();

        // 4) RefreshToken 쿠키
        ResponseCookie refreshCookie = ResponseCookie.from("refreshToken", rawRefreshToken)
                .httpOnly(true)
                .secure(false)
                .path("/")
                .sameSite("Lax")
                .maxAge(Duration.ofDays(14))
                .build();

        response.addHeader("Set-Cookie", accessCookie.toString());
        response.addHeader("Set-Cookie", refreshCookie.toString());
        return accessToken;
    }

    @Transactional
    public Member getOrCreateKakaoMember(String kakaoId, String nickname, String profileImgUrl) {

        return memberRepository
                .findByTypeAndProviderId(Member.LoginType.KAKAO, kakaoId)
                .map(member -> {
                    // 로그인 때마다 최신 프로필 동기화
                    member.updateSocialProfile(nickname, profileImgUrl);
                    return member;
                })
                .orElseGet(() -> {
                    // 최초 소셜 로그인 = 회원가입 처리
                    // email은 카카오에서 scope에 email을 안 받았으니 null 가능
                    // name은 nickname으로 일단 저장
                    Member member = Member.createSocialUser(
                            nickname != null ? nickname : "카카오사용자",
                            null,
                            Member.LoginType.KAKAO,
                            kakaoId,
                            profileImgUrl);

                    return memberRepository.save(member);
                });
    }
}
