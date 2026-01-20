package com.back.domain.member.service;

import java.util.List;

import org.springframework.stereotype.Service;

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

    public boolean deleteApplication(Application application) {
        applicationRepository.delete(application);
        return true;
    }
}
