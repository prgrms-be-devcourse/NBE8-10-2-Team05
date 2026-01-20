package com.back.domain.member.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.back.domain.member.dto.MemberDetailReq;
import com.back.domain.member.dto.MemberDetailRes;
import com.back.domain.member.entity.Member;
import com.back.domain.member.entity.MemberDetail;
import com.back.domain.member.repository.MemberDetailRepository;
import com.back.domain.member.repository.MemberRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MemberDetailService {
    private final MemberDetailRepository memberDetailRepository;
    private final MemberRepository memberRepository;

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
            return CreateDetail(member);
        });
    }

    @Transactional
    public MemberDetail CreateDetail(Member member) {
        return memberDetailRepository.save(MemberDetail.builder().member(member).build());
    }

    @Transactional
    public void modify(Long memberId, MemberDetailReq reqBody) {
        MemberDetail memberDetail = findByMemberId(memberId);
        memberDetail.update(
                reqBody.regionCode(),
                reqBody.marriageStatus(),
                reqBody.income(),
                reqBody.employmentStatus(),
                reqBody.educationLevel(),
                reqBody.specialStatus());
    }
}
