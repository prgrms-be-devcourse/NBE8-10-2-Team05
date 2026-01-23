package com.back.domain.member.bookmark.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import com.back.domain.member.bookmark.service.BookmarkService;

import io.jsonwebtoken.Jwt;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class ApiV1BookmarkControllerTest {

    @Autowired
    private BookmarkService bookmarkService;

    @Autowired
    private MockMvc mvc;

    @Test
    @DisplayName("북마크 검색")
    void getBookmarksByApplicantId() throws Exception {

        Jwt jwt = null; // TODO: test token

        ResultActions resultActions = mvc.perform(
                        get("/api/v1/member/bookmark/welfare-bookmarks").header("Authorization", "Bearer " + jwt))
                .andDo(print());

        // TODO: 응답 결과(andExpect) 구현

    }
}
