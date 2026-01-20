package com.back.domain.welfare.estate.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.List;
import java.util.stream.IntStream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.back.domain.welfare.estate.dto.EstateDto;
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

    @MockitoBean
    private EstateService estateService;

    @Value("${custom.api.estate.url}")
    String apiUrl;

    @Value("${custom.api.estate.key}")
    String apiKey;

    @Test
    @DisplayName("mockResponse 테스트")
    void t0() {
        EstateFetchResponseDto responseDto = mockResponse();

        assertNotNull(responseDto, "ResponseDto가 null입니다.");
        assertNotNull(responseDto.response(), "ResponseDto.response가 null입니다.");
        assertNotNull(responseDto.response().body(), "ResponseDto.response.body가 null입니다.");

        EstateFetchResponseDto.Response.BodyDto body = responseDto.response().body();

        assertFalse(body.numOfRows().isBlank(), "numOfRows가 비어있습니다.");
        assertFalse(body.pageNo().isBlank(), "pageNo가 비어있습니다.");
        assertFalse(body.totalCount().isBlank(), "totalCount가 비어있습니다.");

        assertEquals(9, body.items().size());
    }

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

    private EstateFetchResponseDto mockResponse() {
        List<EstateDto> mockItems = IntStream.range(0, 9)
                .mapToObj(i -> EstateDto.builder().build()) // EstateDto에도 @Builder가 있어야 합니다.
                .toList();

        EstateFetchResponseDto.Response.BodyDto body = new EstateFetchResponseDto.Response.BodyDto(
                "10", // numOfRows
                "1", // pageNo
                "9", // totalCount
                mockItems);

        EstateFetchResponseDto.Response.HeaderDto header =
                new EstateFetchResponseDto.Response.HeaderDto("00", "NORMAL SERVICE.");

        return EstateFetchResponseDto.builder()
                .response(new EstateFetchResponseDto.Response(header, body))
                .build();
    }
}
