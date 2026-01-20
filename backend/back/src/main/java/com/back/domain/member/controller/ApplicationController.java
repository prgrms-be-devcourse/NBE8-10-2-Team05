package com.back.domain.member.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.back.domain.member.dto.AddApplicationResponseDto;
import com.back.domain.member.dto.DeleteApplicationResponseDto;
import com.back.domain.member.entity.Application;
import com.back.domain.member.entity.Member;
import com.back.domain.member.service.ApplicationService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/member")
@RequiredArgsConstructor
public class ApplicationController {

    private final ApplicationService applicationService;

    @GetMapping("/welfare-applications")
    public ResponseEntity<List<Application>> getApplicationList() {
        Member member = new Member(); // TODO: jwt에서 멤버 추출

        applicationService.getApplicationList(member);

        return ResponseEntity.ok(applicationService.getApplicationList(member));
    }

    @PostMapping("/welfare-application/{id}")
    public ResponseEntity<AddApplicationResponseDto> addApplication(@PathVariable Integer id) {
        Member member = new Member(); // TODO: jwt에서 멤버 추출

        if (member == null) {
            AddApplicationResponseDto addApplicationResponseDto =
                    new AddApplicationResponseDto(HttpStatus.BAD_REQUEST.value(), "로그인 후 이용해주세요");
            return ResponseEntity.status(addApplicationResponseDto.getStatus()).body(addApplicationResponseDto);
        }

        Application application = applicationService.addApplication(member, id);

        if (application != null) {
            AddApplicationResponseDto addApplicationResponseDto =
                    new AddApplicationResponseDto(HttpStatus.OK.value(), "저장되었습니다!");
            return ResponseEntity.status(addApplicationResponseDto.getStatus()).body(addApplicationResponseDto);
        } else {
            AddApplicationResponseDto addApplicationResponseDto =
                    new AddApplicationResponseDto(HttpStatus.INTERNAL_SERVER_ERROR.value(), "저장 중 문제가 발생했습니다.");
            return ResponseEntity.status(addApplicationResponseDto.getStatus()).body(addApplicationResponseDto);
        }
    }

    @PutMapping("/welfare-application/{id}")
    public ResponseEntity<DeleteApplicationResponseDto> deleteAplication(@PathVariable long id) {
        Member member = new Member(); // TODO: jwt로 멤버 추출
        DeleteApplicationResponseDto deleteApplicationResponseDto = applicationService.deleteApplication(member, id);

        return ResponseEntity.status(deleteApplicationResponseDto.getCode()).body(deleteApplicationResponseDto);
    }
}
