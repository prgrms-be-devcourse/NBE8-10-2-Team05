package com.back.domain.welfare.estate.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.back.domain.welfare.estate.entity.Estate;

public interface EstateRepository extends JpaRepository<Estate, Integer> {
    List<Estate> findByBrtcNmContaining(String sido);

    List<Estate> findByBrtcNmContainingAndSignguNmContaining(String sido, String signguNm);
}
