package com.JobAyong.controller;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.JobAyong.dto.ResumeRequest;
import com.JobAyong.dto.ResumeResponse;
import com.JobAyong.dto.ResumeEvalRequest;
import com.JobAyong.dto.ResumeEvalResponse;
import com.JobAyong.entity.Resume;
import com.JobAyong.entity.ResumeEval;
import com.JobAyong.entity.User;
import com.JobAyong.service.ResumeService;
import com.JobAyong.repository.UserRepository;

@RestController
@RequestMapping("/api/resume")
public class ResumeController {

    private final ResumeService resumeService;
    private final UserRepository userRepository;

    @Autowired
    public ResumeController(ResumeService resumeService, UserRepository userRepository) {
        this.resumeService = resumeService;
        this.userRepository = userRepository;
    }

    @PostMapping
    public ResponseEntity<ResumeResponse> createResume(@RequestBody ResumeRequest request) {
        // User 조회 (예시)
        User user = userRepository.findByEmail(request.getUserEmail())
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
        Resume resume = resumeService.toResumeEntity(request, user);
        Resume saved = resumeService.save(resume);
        ResumeResponse response = resumeService.toResumeResponse(saved);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{resumeId}")
    public ResponseEntity<ResumeResponse> getResume(@PathVariable Integer resumeId) {
        Optional<Resume> found = resumeService.findByResumeId(resumeId);
        return found.map(resumeService::toResumeResponse)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    public List<ResumeResponse> getAllResumes() {
        return resumeService.findAllResume().stream()
                .map(resumeService::toResumeResponse)
                .collect(Collectors.toList());
    }

    @DeleteMapping("/{resumeId}")
    public ResponseEntity<Void> deleteResume(@PathVariable Integer resumeId) {
        resumeService.deleteByResumeId(resumeId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/eval")
    public ResponseEntity<ResumeEvalResponse> createResumeEval(@RequestBody ResumeEvalRequest request) {
        // User, Resume 조회 (예시)
        User user = userRepository.findByEmail(request.getUserEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));
        Resume resume = resumeService.findByResumeId(request.getResumeId())
                .orElseThrow(() -> new RuntimeException("Resume not found"));
        ResumeEval eval = resumeService.toResumeEvalEntity(request, resume, user);
        ResumeEval saved = resumeService.save(eval);
        ResumeEvalResponse response = resumeService.toResumeEvalResponse(saved);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/eval/{resumeEvalId}")
    public ResponseEntity<ResumeEvalResponse> getResumeEval(@PathVariable Integer resumeEvalId) {
        Optional<ResumeEval> found = resumeService.findByResumeEvalId(resumeEvalId);
        return found.map(resumeService::toResumeEvalResponse)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/eval")
    public List<ResumeEvalResponse> getAllResumeEvals() {
        return resumeService.findAllResumeEval().stream()
                .map(resumeService::toResumeEvalResponse)
                .collect(Collectors.toList());
    }

    @DeleteMapping("/eval/{resumeEvalId}")
    public ResponseEntity<Void> deleteResumeEval(@PathVariable Integer resumeEvalId) {
        resumeService.deleteById(resumeEvalId);
        return ResponseEntity.noContent().build();
    }
}
