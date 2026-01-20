package com.back.domain.welfare.estate.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.back.domain.welfare.estate.dto.EstateFetchRequestDto;
import com.back.domain.welfare.estate.dto.EstateFetchResponseDto;
import com.back.domain.welfare.estate.entity.Estate;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Transactional
class EstateServiceTest {
    @Autowired
    private MockMvc mvc;

    @Autowired
    private EstateService estateService;

    @Value("${custom.api.estate.url}")
    String apiUrl;

    @Value("${custom.api.estate.key}")
    String apiKey;

    @Test
    @DisplayName("fetchEstatePage 테스트")
    void t1() throws Exception {
        EstateFetchRequestDto requestDto = EstateFetchRequestDto.builder().build();
        EstateFetchResponseDto responseDto = estateService.fetchEstatePage(requestDto, 100, 1);

        assertNotNull(responseDto, "ResponseDto가 null입니다.");
        assertNotNull(responseDto.response(), "ResponseDto.response가 null입니다.");
        assertNotNull(responseDto.response().body(), "ResponseDto.response.body가 null입니다.");

        EstateFetchResponseDto.Response.BodyDto body = responseDto.response().body();

        assertFalse(body.numOfRows().isBlank(), "numOfRows가 비어있습니다.");
        assertFalse(body.pageNo().isBlank(), "pageNo가 비어있습니다.");
        assertFalse(body.totalCount().isBlank(), "totalCount가 비어있습니다.");
        assertNotNull(body.items(), "items 리스트 자체가 null입니다.");
    }

    @Test
    @DisplayName("saveEstateList 테스트")
    void t2() {
        EstateFetchRequestDto requestDto = EstateFetchRequestDto.builder().build();
        EstateFetchResponseDto responseDto = estateService.fetchEstatePage(requestDto, 10, 1);

        int totalCnt = Integer.parseInt(responseDto.response().body().totalCount());
        int rows = Math.min(totalCnt, 10);

        List<Estate> estateList = estateService.saveEstateList(responseDto);

        assertEquals(estateList.size(), rows);
    }

    @Test
    @DisplayName("fetchEstateList 테스트")
    void t3() {
        EstateFetchRequestDto requestDto = EstateFetchRequestDto.builder().build();
        EstateFetchResponseDto responseDto = estateService.fetchEstateList(requestDto);

        int totalCnt = Integer.parseInt(responseDto.response().body().totalCount());
        List<Estate> estateList = estateService.saveEstateList(responseDto);

        assertEquals(estateList.size(), totalCnt);
    }
}
