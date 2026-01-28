package com.back.global.springBatch.estate;

import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.infrastructure.item.ItemProcessor;
import org.springframework.stereotype.Component;

import com.back.domain.welfare.estate.dto.EstateDto;
import com.back.domain.welfare.estate.entity.Estate;

@Component
@StepScope
public class EstateApiItemProcessor implements ItemProcessor<EstateDto, Estate> {
    @Override
    public Estate process(EstateDto estateDto) throws Exception {
        return new Estate(estateDto);
    }
}
