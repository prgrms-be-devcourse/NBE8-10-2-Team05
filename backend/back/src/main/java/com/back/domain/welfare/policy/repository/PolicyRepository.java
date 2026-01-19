package com.back.domain.welfare.policy.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.back.domain.welfare.policy.entity.Policy;

public interface PolicyRepository extends JpaRepository<Policy, Integer> {}
