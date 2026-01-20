package com.back.domain.welfare.center.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.back.domain.welfare.center.entity.Lawyer;

public interface LawyerRepository extends JpaRepository<Lawyer, Long> {
    boolean existsByNameAndCorporation(String name, String corporation);
}
