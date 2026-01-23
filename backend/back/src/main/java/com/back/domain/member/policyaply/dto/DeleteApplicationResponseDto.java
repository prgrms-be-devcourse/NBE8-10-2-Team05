package com.back.domain.member.policyaply.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DeleteApplicationResponseDto {
    int code;
    String message;

    public DeleteApplicationResponseDto(int code, String message) {
        this.code = code;
        this.message = message;
    }
}
