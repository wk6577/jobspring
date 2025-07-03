package com.JobAyong.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

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

    /**
     * 파일에서 자소서 생성 및 저장
     */
    @PostMapping("/file")
    public ResponseEntity<?> createResumeFromFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "resumeTitle", required = false) String resumeTitle) {
        
        try {
            // Authentication에서 사용자 정보 가져오기
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String userEmail = authentication.getName();
            
            System.out.println("=== 자소서 파일 업로드 API 호출 ===");
            System.out.println("파일명: " + file.getOriginalFilename());
            System.out.println("사용자 이메일: " + userEmail);
            System.out.println("자소서 제목: " + resumeTitle);
            
            // 파일 검증
            if (file.isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(createErrorResponse("파일이 비어있습니다."));
            }
            
            System.out.println("사용자 조회 시작...");
            // User 조회
            User user = userRepository.findByEmail(userEmail)
                    .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다: " + userEmail));
            
            System.out.println("사용자 조회 완료: " + user.getEmail());
            
            // GPT를 통해 자소서 추출 및 저장
            System.out.println("자소서 생성 시작...");
            Resume saved = resumeService.createResumeFromFile(file, user, resumeTitle);
            
            System.out.println("=== 자소서 파일 업로드 API 완료 ===");
            return ResponseEntity.ok(createSuccessResponse("자소서가 성공적으로 생성되었습니다.", saved));
            
        } catch (Exception e) {
            System.err.println("=== 자소서 파일 업로드 API 실패 ===");
            System.err.println("오류 클래스: " + e.getClass().getSimpleName());
            System.err.println("오류 메시지: " + e.getMessage());
            e.printStackTrace();
            
            String errorMessage = "자기소개서 파일 저장에 실패했습니다: " + e.getMessage();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse(errorMessage));
        }
    }

    /**
     * 파일에서 자소서 텍스트만 추출하여 반환 (저장하지 않음)
     */
    @PostMapping("/extract")
    public ResponseEntity<?> extractResumeFromFile(@RequestParam("file") MultipartFile file) {
        try {
            if (file.isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(createErrorResponse("파일이 비어있습니다."));
            }
            
            String extractedResumeText = resumeService.extractAndProcessResumeFromFile(file);
            return ResponseEntity.ok(createSuccessResponse("자소서 텍스트 추출 완료", extractedResumeText));
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("자소서 추출 실패: " + e.getMessage()));
        }
    }

    /**
     * 파일에서 생 텍스트만 추출하여 반환 (GPT 처리 없음)
     */
    @PostMapping("/raw-text")
    public ResponseEntity<?> extractRawTextFromFile(@RequestParam("file") MultipartFile file) {
        try {
            if (file.isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(createErrorResponse("파일이 비어있습니다."));
            }
            
            String rawText = resumeService.extractTextFromFile(file);
            return ResponseEntity.ok(createSuccessResponse("생 텍스트 추출 완료", rawText));
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("텍스트 추출 실패: " + e.getMessage()));
        }
    }

    /**
     * 자소서 평가 생성
     */
    @PostMapping("/eval")
    public ResponseEntity<ResumeEvalResponse> createResumeEval(@RequestBody ResumeEvalRequest request) {
        System.out.println("=== 자소서 평가 생성 API 호출 ===");
        System.out.println("요청 데이터:");
        System.out.println("resumeId: " + request.getResumeId());
        System.out.println("resumeEvalTitle: \"" + request.getResumeEvalTitle() + "\"");
        System.out.println("resumeEvalTitle 타입: " + (request.getResumeEvalTitle() != null ? request.getResumeEvalTitle().getClass().getSimpleName() : "null"));
        System.out.println("resumeEvalTitle 길이: " + (request.getResumeEvalTitle() != null ? request.getResumeEvalTitle().length() : "null"));
        System.out.println("resumeEvalVersion: " + request.getResumeEvalVersion());
        
        // Authentication에서 사용자 정보 가져오기
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userEmail = authentication.getName();
        System.out.println("사용자 이메일: " + userEmail);
        
        // User, Resume 조회
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Resume resume = resumeService.findByResumeId(request.getResumeId())
                .orElseThrow(() -> new RuntimeException("Resume not found"));
        
        System.out.println("Resume 정보:");
        System.out.println("resumeId: " + resume.getResumeId());
        System.out.println("resumeTitle: \"" + resume.getResumeTitle() + "\"");
        
        ResumeEval eval = resumeService.toResumeEvalEntity(request, resume, user);
        
        System.out.println("생성된 ResumeEval 엔티티:");
        System.out.println("resumeEvalTitle: \"" + eval.getResumeEvalTitle() + "\"");
        
        ResumeEval saved = resumeService.save(eval);
        
        System.out.println("저장된 ResumeEval:");
        System.out.println("resumeEvalId: " + saved.getResumeEvalId());
        System.out.println("resumeEvalTitle: \"" + saved.getResumeEvalTitle() + "\"");
        System.out.println("=== 자소서 평가 생성 완료 ===");
        
        ResumeEvalResponse response = resumeService.toResumeEvalResponse(saved);
        return ResponseEntity.ok(response);
    }

    /**
     * 특정 자소서의 최대 평가 버전 조회
     */
    @GetMapping("/eval/max-version/{resumeId}")
    public ResponseEntity<Integer> getMaxVersionByResumeId(@PathVariable Integer resumeId) {
        Integer maxVersion = resumeService.getMaxVersionByResumeId(resumeId);
        return ResponseEntity.ok(maxVersion);
    }

    /**
     * OpenAI API 키 테스트 엔드포인트
     */
    @GetMapping("/test-api")
    public ResponseEntity<?> testOpenAIAPI() {
        try {
            // 간단한 테스트 프롬프트로 API 키 확인
            String testResult = resumeService.testGPTConnection();
            return ResponseEntity.ok(createSuccessResponse("OpenAI API 연결 테스트 성공", testResult));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("OpenAI API 연결 테스트 실패: " + e.getMessage()));
        }
    }

    // 응답 헬퍼 메서드들
    private Map<String, Object> createSuccessResponse(String message, Object data) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", message);
        response.put("data", data);
        return response;
    }

    private Map<String, Object> createErrorResponse(String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("message", message);
        response.put("data", null);
        return response;
    }
}
