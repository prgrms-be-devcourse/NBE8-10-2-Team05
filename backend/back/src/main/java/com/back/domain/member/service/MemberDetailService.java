package com.back.domain.member.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.back.domain.member.dto.MemberDetailReq;
import com.back.domain.member.dto.MemberDetailRes;
import com.back.domain.member.entity.MemberDetail;
import com.back.domain.member.repository.MemberDetailRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MemberDetailService {
    private final MemberDetailRepository memberDetailRepository;

    @Transactional(readOnly = true)
    public MemberDetailRes getDetail(Long memberId) {
        MemberDetail memberDetail = findByMemberId(memberId);
        return new MemberDetailRes(memberDetail);
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

    @Transactional(readOnly = true)
    public MemberDetail findByMemberId(Long id) {
        return memberDetailRepository.findByMemberId(id).get();
    }
}
