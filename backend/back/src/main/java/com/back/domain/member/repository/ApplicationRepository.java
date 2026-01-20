package com.back.domain.member.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.back.domain.member.entity.Application;

public interface ApplicationRepository extends JpaRepository<Application, Long> {

    List<Application> getApplicationsById(Long id);

    Application getApplicationById(Long id);
}
