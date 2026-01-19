package com.back.domain.welfare.estate.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Transactional
class EstateServiceTest {
    @Autowired
    private MockMvc mvc;

    @Autowired
    private EstateService estateService;

    @Test
    @DisplayName("국토교통부_마이홈포털 공공주택 모집공고 조회 서비스 API 테스트")
    void t1() throws Exception {
        String apiUrl = "http://apis.data.go.kr/1613000/HWSPR02/rsdtRcritNtcList";
        String apiKey = "SgWKaXt9FYqTmctvceuHwbb8QlVKEtphZ0fFDtIb40qm5UvMNPGkyIXAiufeOYnyWu39WYNP5W+4a1T+KPHRuw==";

        ResultActions resultActions =
                mvc.perform(get(apiUrl).param("serviceKey", apiKey)).andDo(print());

        resultActions.andExpect(status().isOk());
    }

    @Test
    @DisplayName(" EstateFetchResponseDto 테스트")
    void t2() {}
}
