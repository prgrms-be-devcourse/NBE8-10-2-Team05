package com.back.domain.welfare.estate.entity;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.springframework.stereotype.Component;

import com.back.domain.welfare.estate.dto.EstateRegionDto;
import com.back.domain.welfare.estate.repository.EstateRepository;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class EstateRegionCache {
    @Getter
    private final List<EstateRegionDto> regionList = new CopyOnWriteArrayList<>();

    private final EstateRepository estateRepository;

    public void init() {
        this.regionList.clear();

        List<Estate> parents = estateRepository.findDistinctBrtcNmBy();
        parents.forEach(p -> regionList.add(new EstateRegionDto(p.getBrtcNm(), null, 1)));

        List<Estate> children = estateRepository.findDistinctBrtcNmAndSignguNmBy();
        children.forEach(c -> {
            String parentName = c.getBrtcNm();
            String childName = c.getSignguNm();
            if (childName != null) {
                regionList.add(new EstateRegionDto(childName, parentName, 2));
            }
        });
    }
}
