package com.back.domain.member.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.back.domain.member.entity.Application;
import com.back.domain.member.entity.Member;
import com.back.domain.member.repository.ApplicationRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ApplicationService {

    private final ApplicationRepository applicationRepository;

    public List<Application> getApplicationList(Member member) {
        return applicationRepository.getApplicationsById(member.getId());
    }

    public Application addApplication(Member member, int id) {}

    public boolean deleteApplication(Application application) {}
}
