package com.back.domain.welfare.estate.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.back.domain.welfare.estate.entity.Estate;

@Repository
public interface EstateRepository extends JpaRepository<Estate, Integer> {
    List<Estate> findByBrtcNmContaining(String sido);

    List<Estate> findByBrtcNmContainingAndSignguNmContaining(String sido, String signguNm);

    @Query("SELECT DISTINCT e.brtcNm FROM Estate e")
    List<String> findDistinctBrtcNmBy();

    @Query("SELECT DISTINCT e.brtcNm, e.signguNm FROM Estate e")
    List<Object[]> findDistinctBrtcNmAndSignguNmBy();
}
