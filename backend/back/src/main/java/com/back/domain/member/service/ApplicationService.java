package com.back.domain.member.service;

import org.springframework.stereotype.Service;

import com.back.domain.member.repository.ApplicationRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ApplicationService {

    private ApplicationRepository applicationRepository;
}
