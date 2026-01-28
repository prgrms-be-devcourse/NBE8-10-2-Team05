package com.back.domain.welfare.estate.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.back.domain.welfare.estate.entity.Estate;

public interface EstateRepository extends JpaRepository<Estate, Integer> {
    @Query("SELECT e FROM Estate e WHERE SUBSTRING(e.pnu, 1, 5) = :signguCode")
    List<Estate> searchBySignguCode(String signguCode);
}
