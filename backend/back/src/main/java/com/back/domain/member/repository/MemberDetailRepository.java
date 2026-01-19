package com.back.domain.member.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.back.domain.member.entity.MemberDetail;

public interface MemberDetailRepository extends JpaRepository<MemberDetail, Long> {}
