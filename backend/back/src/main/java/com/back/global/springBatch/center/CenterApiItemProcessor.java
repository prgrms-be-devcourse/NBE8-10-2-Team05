package com.back.global.springBatch.center;

import org.springframework.batch.infrastructure.item.ItemProcessor;
import org.springframework.stereotype.Component;

import com.back.domain.welfare.center.center.dto.CenterApiResponseDto;
import com.back.domain.welfare.center.center.entity.Center;

@Component
public class CenterApiItemProcessor implements ItemProcessor<CenterApiResponseDto.CenterDto, Center> {
    @Override
    public Center process(CenterApiResponseDto.CenterDto centerDto) {
        return Center.dtoToEntity(centerDto);
    }
}
