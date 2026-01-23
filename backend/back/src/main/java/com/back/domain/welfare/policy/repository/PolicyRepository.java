package com.back.domain.welfare.policy.repository;

import java.util.Set;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.back.domain.welfare.policy.entity.Policy;

@Repository
public interface PolicyRepository extends JpaRepository<Policy, Integer> {
    Policy findPolicyById(int policyId);

    // 데이터 중복방지를 위해 policyNum만 가져옴
    @Query("select p.plcyNo from Policy p where p.plcyNo in :plcyNos")
    Set<String> findExistingPlcyNos(@Param("plcyNos") Set<String> plcyNos);
}
