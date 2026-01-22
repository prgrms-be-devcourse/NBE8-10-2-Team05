package com.back.domain.welfare.policy.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.back.domain.welfare.policy.entity.Policy;

@Repository
public interface PolicyRepository extends JpaRepository<Policy, Integer> {
    Policy findPolicyById(int policyId);

    boolean existsByPlcyNo(String plcyNo);
}
