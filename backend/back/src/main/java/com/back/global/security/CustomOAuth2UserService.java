package com.back.global.security;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.back.domain.member.member.entity.Member;
import com.back.domain.member.member.service.MemberService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomOAuth2UserService extends DefaultOAuth2UserService {
    private final MemberService memberService;

    // 카카오톡 로그인이 성공할 때 마다 이 함수가 실행된다.
    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);

        // 카카오 id (yml에서 user-name-attribute: id 설정했으니 name = id)
        String kakaoId = oAuth2User.getName(); // 예: "47121"

        Map<String, Object> attributes = new HashMap<>(oAuth2User.getAttributes());
        Map<String, Object> properties = (Map<String, Object>) attributes.get("properties");

        String nickname = properties != null ? (String) properties.get("nickname") : null;
        String profileImgUrl = properties != null ? (String) properties.get("profile_image") : null;

        // 잘 받아와지는지 로그 체크
        // log.info("kakaoId={}, nickname={}, profileImgUrl={}", kakaoId, nickname, profileImgUrl);

        // DB에서 회원 조회 or 생성
        Member member = memberService.getOrCreateKakaoMember(kakaoId, nickname, profileImgUrl);

        // SuccessHandler에서 쿠키 발급할 때 memberId가 필요하므로
        // attributes에 우리 memberId를 넣어둔다.
        attributes.put("memberId", member.getId());

        List<GrantedAuthority> authorities =
                List.of(new SimpleGrantedAuthority("ROLE_" + member.getRole().name()));

        // username은 "로그인 식별자" 규칙으로: KAKAO__{providerId}
        // (여기서는 카카오니까 고정으로 KAKAO__{kakaoId}도 OK)
        return new SecurityUser(
                member.getId(),
                buildUsername(member), // EMAIL이면 email / 소셜이면 TYPE__providerId
                "", // 소셜은 password 없음
                member.getName(), // 닉네임이 member.name에 들어가 있을 거라 그대로 사용
                authorities);
    }

    /**
     * username을 “로그인 식별자”로 통일
     * - EMAIL: email
     * - 소셜: {type}__{providerId} (ex. KAKAO__47121)
     */
    private String buildUsername(Member member) {
        if (member.getType() == Member.LoginType.EMAIL) {
            return member.getEmail();
        }
        return member.getType().name() + "__" + member.getProviderId();
    }
}
