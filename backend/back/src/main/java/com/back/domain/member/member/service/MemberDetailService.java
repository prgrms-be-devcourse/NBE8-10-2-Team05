package com.back.domain.member.member.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.back.domain.member.geo.entity.AddressDto;
import com.back.domain.member.geo.service.GeoService;
import com.back.domain.member.member.dto.MemberDetailReq;
import com.back.domain.member.member.dto.MemberDetailRes;
import com.back.domain.member.member.entity.Member;
import com.back.domain.member.member.entity.MemberDetail;
import com.back.domain.member.member.repository.MemberDetailRepository;
import com.back.domain.member.member.repository.MemberRepository;
import com.back.global.exception.ServiceException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MemberDetailService {
    private final MemberDetailRepository memberDetailRepository;
    private final MemberRepository memberRepository;
    private final GeoService geoService;

    @Transactional
    public MemberDetailRes getDetail(Long memberId) {
        MemberDetail memberDetail = findByMemberId(memberId);
        return new MemberDetailRes(memberDetail);
    }

    @Transactional
    public MemberDetail findByMemberId(Long memberId) {

        return memberDetailRepository.findById(memberId).orElseGet(() -> {
            // 상세 정보가 없다?, 진짜 유저 자체가 없는지 확인.
            Member member = memberRepository
                    .findById(memberId)
                    // 유저 자체가 없다? 예외처리
                    .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));
            // 유저는 있는데 상세 정보가 없다? 빈 상세 정보 만들어 저장
            return createDetail(member);
        });
    }

    @Transactional
    public MemberDetail createDetail(Member member) {
        return memberDetailRepository.save(MemberDetail.builder().member(member).build());
    }

    @Transactional
    public void modify(Long memberId, MemberDetailReq reqBody) {
        MemberDetail memberDetail = findByMemberId(memberId);
        Member member = memberDetail.getMember();
        updateMemberInfo(member, reqBody);
        memberDetail.update(
                reqBody.regionCode(),
                reqBody.marriageStatus(),
                reqBody.income(),
                reqBody.employmentStatus(),
                reqBody.educationLevel(),
                reqBody.specialStatus());
    }

    @Transactional
    public void updateAddress(Long memberId, AddressDto addressDto) {
        MemberDetail memberDetail = findByMemberId(memberId);

        AddressDto enrichedAddressDto = geoService.getGeoCode(addressDto);
        // 입력된 불완전한 addressDto -> geoService 이용, 엄밀한 주소 데이터로 보충해줌
        memberDetail.updateAddress(enrichedAddressDto);
    }

    public void updateMemberInfo(Member member, MemberDetailReq req) {
        // reqBody의 멤버 정보값이 null이다? -> 기존 멤버 엔티티의 값 유지
        String newName = (req.name() != null ? req.name() : member.getName());
        String newEmail = (req.email() != null ? req.email() : member.getEmail());
        Integer newRrnFront = (req.rrnFront() != null ? req.rrnFront() : member.getRrnFront());
        Integer newRrnBackFirst = (req.rrnBackFirst() != null ? req.rrnBackFirst() : member.getRrnBackFirst());

        // 이메일이 변경되는 경우, 중복 체크 수행
        if (!member.getEmail().equals(newEmail)) {
            if (memberRepository.existsByEmail(newEmail)) {
                throw new ServiceException("MEMBER_409", "이미 존재하는 이메일입니다");
            }
        }
        member.updateInfo(newName, newEmail, newRrnFront, newRrnBackFirst);
    }
}
