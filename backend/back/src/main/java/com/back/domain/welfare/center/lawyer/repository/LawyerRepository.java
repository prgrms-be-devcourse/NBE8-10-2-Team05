package com.back.domain.welfare.center.lawyer.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.back.domain.welfare.center.lawyer.entity.Lawyer;

public interface LawyerRepository extends JpaRepository<Lawyer, Long> {
    boolean existsByNameAndCorporation(String name, String corporation);
    // 이름, 법인으로 중복체크

    Page<Lawyer> findByDistrictArea1AndDistrictArea2Containing(String area1, String area2, Pageable pageable);
}
