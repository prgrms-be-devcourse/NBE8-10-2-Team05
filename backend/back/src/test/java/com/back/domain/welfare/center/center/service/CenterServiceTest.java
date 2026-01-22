package com.back.domain.welfare.center.center.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

import com.back.domain.welfare.center.center.dto.CenterRequestDto;
import com.back.domain.welfare.center.center.dto.CenterResponseDto;
import com.back.domain.welfare.center.center.repository.CenterRepository;
import com.back.domain.welfare.center.entity.Center;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class CenterServiceTest {
    @Autowired
    private CenterService centerService;

    @Autowired
    private CenterRepository centerRepository;

    @MockitoBean
    private CenterApiService centerApiService;

    @Test
    @DisplayName("getCenterData 테스트")
    void t1() {
        CenterRequestDto requestDto = CenterRequestDto.from(10, 1);
        CenterResponseDto responseDto = createResponseData();
        Mockito.when(centerApiService.fetchCenter(requestDto)).thenReturn(responseDto);

        List<Center> centerList = centerService.getCenterData();

        long count = centerRepository.count();

        assertThat(centerList).isNotEmpty();
        assertThat(count).isEqualTo(centerList.size());
    }

    private CenterResponseDto createResponseData() {
        List<CenterResponseDto.CenterDto> mockList = List.of(
                new CenterResponseDto.CenterDto(
                        1, "강원", "강릉종합사회복지관", "강원도 강릉시 강변로 510, 관리사무소건물 2층", "033-653-6375", "월정사복지재단", "사회복지법인"),
                new CenterResponseDto.CenterDto(
                        2, "강원", "명륜종합사회복지관", "강원도 원주시 예술관길 25", "033-762-8131", "성불복지회", "사회복지법인"),
                new CenterResponseDto.CenterDto(
                        3, "강원", "밥상공동체종합사회복지관", "강원도 원주시 일산로 81-2", "033-766-4933", "밥상공동체복지재단", "사회복지법인(직영)"),
                new CenterResponseDto.CenterDto(
                        4, "강원", "삼척시종합사회복지관", "강원도 삼척시 원당로 2길 72-6", "033-573-6168", "원주가톨릭사회복지회", "사회복지법인"),
                new CenterResponseDto.CenterDto(
                        5, "강원", "속초종합사회복지관", "강원도 속초시 먹거리길19", "033-631-8761", "대한불교조계종신흥사복지재단", "사회복지법인"),
                new CenterResponseDto.CenterDto(
                        6, "강원", "영월군종합사회복지관", "강원도 영월군 영월읍 단종로 12", "033-375-4600", "원주가톨릭사회복지회", "사회복지법인"),
                new CenterResponseDto.CenterDto(
                        7, "강원", "원주가톨릭종합사회복지관", "강원도 원주시 봉산로 103", "033-731-1121", "원주가톨릭사회복지회", "사회복지법인(직영)"),
                new CenterResponseDto.CenterDto(
                        8, "강원", "원주종합사회복지관", "강원도 원주시 육판길 1", "033-732-4006", "원주종합사회복지재단", "사회복지법인"),
                new CenterResponseDto.CenterDto(
                        9, "강원", "월드비전동해종합사회복지관", "강원도 동해시 전천로 273-10", "033-533-8247", "월드비전", "사회복지법인"),
                new CenterResponseDto.CenterDto(
                        10, "강원", "월드비전춘천종합사회복지관", "강원도 춘천시 근화길 95", "033-254-7244", "월드비전", "사회복지법인(직영)"));

        return new CenterResponseDto(1, 10, 481, 10, 481, mockList);
    }
}
