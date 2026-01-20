package com.back.domain.member.service;

import java.util.List;
import java.util.Objects;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.back.domain.member.dto.DeleteApplicationResponseDto;
import com.back.domain.member.entity.Application;
import com.back.domain.member.entity.Member;
import com.back.domain.member.repository.ApplicationRepository;
import com.back.domain.welfare.policy.entity.Policy;
import com.back.domain.welfare.policy.repository.PolicyRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ApplicationService {

    private final ApplicationRepository applicationRepository;

    private final PolicyRepository policyRepository;

    public List<Application> getApplicationList(Member member) {
        return applicationRepository.getApplicationsById(member.getId());
    }

    public Application addApplication(Member member, int policyId) {
        Application application = new Application();

        Policy policy = policyRepository.findPolicyById(policyId);
        application.setPolicy(policy);
        application.setApplicant(member);

        applicationRepository.save(application);
        return application;
    }

    public DeleteApplicationResponseDto deleteApplication(Member member, long applicationId) {
        Application application = applicationRepository.getApplicationById(applicationId);

        if (application == null) {
            return new DeleteApplicationResponseDto(HttpStatus.NOT_FOUND.value(), "신청 내역을 찾지 못했습니다.");
        }

        if (!Objects.equals(application.getApplicant().getId(), member.getId())) {
            return new DeleteApplicationResponseDto(HttpStatus.UNAUTHORIZED.value(), "삭제 권한이 없습니다.");
        }

        applicationRepository.delete(application);
        return new DeleteApplicationResponseDto(HttpStatus.OK.value(), "성공적으로 삭제되었습니다.");
    }
}
