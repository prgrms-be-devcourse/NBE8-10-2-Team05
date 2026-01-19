package com.back.domain.welfare.estate.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.back.domain.welfare.estate.entity.Estate;

public interface EstateRepository extends JpaRepository<Estate, Integer> {}
