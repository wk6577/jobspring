package com.JobAyong.controller;

import com.JobAyong.dto.InterviewArchiveResponse;
import com.JobAyong.service.InterviewArchiveService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/interview-archives")
public class InterviewArchiveController {

    private final InterviewArchiveService interviewArchiveService;

    @GetMapping
    public List<InterviewArchiveResponse> getAllArchives() {
        return interviewArchiveService.getAllArchives();
    }
}
