package com.JobAyong.controller;

import com.JobAyong.dto.InterviewArchiveResponse;
import com.JobAyong.service.InterviewArchiveService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/interview-archives")
public class InterviewArchiveController {

    private final InterviewArchiveService interviewArchiveService;

    @GetMapping
    public List<InterviewArchiveResponse> getAllArchives() {
        return interviewArchiveService.getAllArchives();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteInterviewArchive(@PathVariable int id) {
        interviewArchiveService.deleteById(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{id}/questions-answers")
    public ResponseEntity<List<Map<String, Object>>> getQuestionsAndAnswers(@PathVariable int id) {
        List<Map<String, Object>> questionsAndAnswers = interviewArchiveService.getQuestionsAndAnswers(id);
        return ResponseEntity.ok(questionsAndAnswers);
    }

    // 관리자 전용, 전체 면접 기록 조회 (권한 필터 없이 전체)
    @GetMapping("/admin/all")
    public ResponseEntity<List<InterviewArchiveResponse>> getAllArchivesForAdmin() {
        List<InterviewArchiveResponse> allArchives = interviewArchiveService.getAllArchivesForAdmin();
        return ResponseEntity.ok(allArchives);
    }
}