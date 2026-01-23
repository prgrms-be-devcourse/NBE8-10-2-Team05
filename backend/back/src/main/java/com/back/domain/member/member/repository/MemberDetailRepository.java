package com.back.domain.member.member.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.back.domain.member.member.entity.MemberDetail;

public interface MemberDetailRepository extends JpaRepository<MemberDetail, Long> {}
