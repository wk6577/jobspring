package com.JobAyong.controller;

import com.JobAyong.dto.createNewInterviewArchiveRequest;
import com.JobAyong.service.InterviewService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/interview")
@RequiredArgsConstructor
public class InterviewController {

    private final InterviewService interviewService;

    /*@apiNote 새로운 모의 면접 기록 생성
    * @author 나세호
    * */
    @PostMapping
    public ResponseEntity<?> createNewInterviewArchive(@RequestBody  createNewInterviewArchiveRequest request){
        interviewService.createArchive(request);
        return ResponseEntity.ok("새로운 모의 면접 기록 생성됨");
    }
}
