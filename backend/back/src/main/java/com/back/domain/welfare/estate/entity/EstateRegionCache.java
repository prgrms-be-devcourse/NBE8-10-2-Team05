package com.back.domain.welfare.estate.entity;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.springframework.stereotype.Component;

import com.back.domain.welfare.estate.dto.EstateRegionDto;
import com.back.domain.welfare.estate.repository.EstateRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class EstateRegionCache {
    private final List<EstateRegionDto> regionList = new CopyOnWriteArrayList<>();
    private final EstateRepository estateRepository;

    public void init() {
        this.regionList.clear();

        List<String> parents = estateRepository.findDistinctBrtcNmBy();
        parents.forEach(p -> regionList.add(new EstateRegionDto(p, null, 1)));

        List<Object[]> children = estateRepository.findDistinctBrtcNmAndSignguNmBy();
        children.forEach(c -> {
            String parentName = (String) c[0];
            String childName = (String) c[1];
            if (childName != null) {
                regionList.add(new EstateRegionDto(childName, parentName, 2));
            }
        });
    }
}
