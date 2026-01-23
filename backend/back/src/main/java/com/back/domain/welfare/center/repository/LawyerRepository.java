package com.back.domain.welfare.center.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.back.domain.welfare.center.entity.Lawyer;

public interface LawyerRepository extends JpaRepository<Lawyer, Long> {
    boolean existsByNameAndCorporation(String name, String corporation);
    // 이름, 법인으로 중복체크

    List<Lawyer> findByDistrictArea1AndDistrictArea2Containing(String area1, String area2);
    // 시/도 단위는 area1와 정확하게 일치해야하고, 군/구 단위는 area2를 포함만 해도 됨.
}
