package com.back.domain.member.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AddApplicationResponseDto {
    int status;
    String message;

    public AddApplicationResponseDto(int status, String message) {
        this.status = status;
        this.message = message;
    }
}
