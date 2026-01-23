package com.back.domain.member.bookmark.dto;

public class BookmarkUpdateResponseDto {
    int code;
    String message;

    public BookmarkUpdateResponseDto(int code, String message) {
        this.code = code;
        this.message = message;
    }
}
