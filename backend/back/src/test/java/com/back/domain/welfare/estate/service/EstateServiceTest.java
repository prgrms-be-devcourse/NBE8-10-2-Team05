package com.back.domain.welfare.estate.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

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
    @DisplayName("국토교통부_마이홈포털 공공주택 모집공고 조회 서비스 API 테스트")
    void t1() throws Exception {
        EstateFetchRequestDto requestDto = EstateFetchRequestDto.builder().build();
        EstateFetchResponseDto responseDto = estateService.fetchEstateList(requestDto);

        assertNotNull(responseDto, "ResponseDto가 null입니다.");
        assertNotNull(responseDto.response(), "ResponseDto.response가 null입니다.");
        assertNotNull(responseDto.response().body(), "ResponseDto.response.body가 null입니다.");

        EstateFetchResponseDto.Response.ResponseDto body =
                responseDto.response().body();

        assertFalse(body.numOfRows().isBlank(), "numOfRows가 비어있습니다.");
        assertFalse(body.pageNo().isBlank(), "pageNo가 비어있습니다.");
        assertFalse(body.totalCount().isBlank(), "totalCount가 비어있습니다.");
        assertNotNull(body.items(), "items 리스트 자체가 null입니다.");
        assertFalse(body.items().isEmpty(), "items 리스트가 비어있습니다.");
    }

    @Test
    @DisplayName("fetchEstateList 테스트")
    void t2() {
        // EstateFetchRequestDto requestDto = new EstateFetchRequestDto();
        // estateService.fetchEstateList();
        //        ResultActions resultActions =
        //            mvc.perform(get(apiUrl).param("serviceKey", apiKey)).andDo(print());
        //
        //        resultActions.andExpect(status().isOk());

    }
}
