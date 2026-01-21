package com.back.domain.member;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import com.back.domain.member.service.BookmarkService;
import com.back.global.test.BaseAuthControllerTest;

@ActiveProfiles("test")
@Transactional
public class ApiV1BookmarkControllerTest extends BaseAuthControllerTest {

    @Autowired
    private BookmarkService bookmarkService;

    @Autowired
    private MockMvc mvc;

    @Test
    @DisplayName("북마크 검색")
    void getBookmarksByApplicantId() throws Exception {

        //        Jwt jwt = null; // TODO: test token

        ResultActions resultActions =
                mvc.perform(get("/api/v1/member/welfare-bookmarks")).andDo(print());

        // TODO: 응답 결과(andExpect) 구현

    }
}
