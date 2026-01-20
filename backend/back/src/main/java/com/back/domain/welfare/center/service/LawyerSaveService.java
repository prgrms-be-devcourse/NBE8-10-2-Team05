package com.back.domain.welfare.center.service;

import java.util.List;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.back.domain.welfare.center.entity.Lawyer;
import com.back.domain.welfare.center.repository.LawyerRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class LawyerSaveService {
    private final LawyerRepository lawyerRepository;

    @Transactional
    public void saveList(List<Lawyer> lawyerList) {
        for (Lawyer lawyer : lawyerList) {
            try {
                lawyerRepository.save(lawyer);
            } catch (DataIntegrityViolationException e) {
            }
        }
    }
}
