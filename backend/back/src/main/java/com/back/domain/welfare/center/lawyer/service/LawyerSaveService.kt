package com.back.domain.welfare.center.lawyer.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import com.back.domain.welfare.center.lawyer.entity.Lawyer;
import com.back.domain.welfare.center.lawyer.repository.LawyerRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class LawyerSaveService {
    private final LawyerRepository lawyerRepository;
    private final TransactionTemplate transactionTemplate;

    @Transactional
    public void saveList(List<Lawyer> lawyerList) {
        for (Lawyer lawyer : lawyerList) {
            transactionTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
            // 현재 실행할 지점에 무조건 새로운 트랜잭션으로 실행하도록 설정
            // -> 개별 노무사의 저장마다 개별 트랜잭션 열리도록해서, 특정 노무사의 저장에서 에러가 나도 전체 리스트에 영향이 가지 않도록

            transactionTemplate.execute(status -> {
                try {
                    if (!lawyerRepository.existsByNameAndCorporation(lawyer.getName(), lawyer.getCorporation())) {
                        // 중복 체크하고 저장
                        lawyerRepository.save(lawyer);
                    }
                } catch (Exception e) {
                    status.setRollbackOnly(); // 에러 나면 이 한 명만 롤백
                }
                return null;
            });
        }
    }
}
