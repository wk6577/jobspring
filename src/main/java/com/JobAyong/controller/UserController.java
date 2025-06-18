package com.JobAyong.controller;

import com.JobAyong.dto.PasswordChangeRequest;
import com.JobAyong.dto.UserSignUpRequest;
import com.JobAyong.dto.UserInfoResponse;
import com.JobAyong.dto.UserUpdateRequest;
import com.JobAyong.entity.*;
import com.JobAyong.repository.InterviewArchiveRepository;
import com.JobAyong.repository.InterviewEvalRepository;
import com.JobAyong.repository.InterviewQuestionRepository;
import com.JobAyong.repository.InterviewAnswerRepository;
import com.JobAyong.service.InterviewService;
import com.JobAyong.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;
    private final InterviewArchiveRepository interviewArchiveRepository;
    private final InterviewEvalRepository interviewEvalRepository;
    private final InterviewQuestionRepository interviewQuestionRepository;
    private final InterviewAnswerRepository interviewAnswerRepository;
    private final InterviewService interviewService;

    @PostMapping("/signup")
    public ResponseEntity<Void> signUp(@RequestBody UserSignUpRequest request) {
        userService.signUp(request);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/check-email")
    public ResponseEntity<Boolean> checkEmailExists(@RequestParam String email) {
        boolean exists = userService.existsByEmail(email);
        return ResponseEntity.ok(exists);
    }

    @GetMapping("/{email}")
    public ResponseEntity<UserInfoResponse> getUserByEmail(@PathVariable String email) {
        try {
            log.info("사용자 정보 조회 요청: {}", email);
            User user = userService.findByEmail(email);
            log.info("조회된 사용자 정보 - 이름: {}, 생년월일: {}, 전화번호: {}, 성별: {}", 
                user.getName(), user.getBirth(), user.getPhoneNumber(), user.getGender());
            
            UserInfoResponse response = new UserInfoResponse(
                user.getEmail(),
                user.getName(),
                user.getBirth() != null ? user.getBirth().toString() : null,
                user.getPhoneNumber(),
                user.getGender() != null ? user.getGender().toString() : null,
                null, // 프로필 이미지는 나중에 구현
                user.getJob(), // 직무 정보
                user.getCompany() // 회사 정보
            );
            
            log.info("응답 데이터 - 생년월일: {}, 전화번호: {}, 성별: {}", 
                response.getBirth(), response.getPhoneNumber(), response.getGender());
            
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            log.error("사용자 정보 조회 실패: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/{email}")
    public ResponseEntity<UserInfoResponse> updateUser(@PathVariable String email, @RequestBody UserUpdateRequest request) {
        try {
            log.info("사용자 정보 수정 요청: {}", email);
            User updatedUser = userService.updateUser(email, request);
            
            UserInfoResponse response = new UserInfoResponse(
                updatedUser.getEmail(),
                updatedUser.getName(),
                updatedUser.getBirth() != null ? updatedUser.getBirth().toString() : null,
                updatedUser.getPhoneNumber(),
                updatedUser.getGender() != null ? updatedUser.getGender().toString() : null,
                null, // 프로필 이미지는 나중에 구현
                updatedUser.getJob(), // 직무 정보
                updatedUser.getCompany() // 회사 정보
            );
            
            log.info("사용자 정보 수정 완료: {}", email);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            log.error("사용자 정보 수정 실패: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/{email}/password")
    public ResponseEntity<Void> changePassword(@PathVariable String email, @RequestBody PasswordChangeRequest request) {
        try {
            log.info("비밀번호 변경 요청: {}", email);
            userService.changePassword(email, request);
            log.info("비밀번호 변경 성공: {}", email);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            log.error("비밀번호 변경 실패: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/{email}")
    public ResponseEntity<Void> deleteUser(@PathVariable String email) {
        if (!userService.existsByEmail(email)) {
            return ResponseEntity.notFound().build();
        }
        userService.delete(email);
        return ResponseEntity.ok().build();
    }
    
    /**
     * 사용자의 모든 평가 목록을 조회합니다.
     * @return 평가 목록
     */
    @GetMapping("/evaluations")
    public ResponseEntity<List<Map<String, Object>>> getEvaluationList() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        
        log.info("사용자 평가 목록 조회 요청: {}", email);
        
        try {
            // 면접 평가 목록 조회
            List<Map<String, Object>> evaluationList = new ArrayList<>();
            List<InterviewArchive> interviewArchives = interviewArchiveRepository.findByUserEmail(email);
            
            for (InterviewArchive archive : interviewArchives) {
                Map<String, Object> evaluation = new HashMap<>();
                evaluation.put("id", archive.getInterviewArchiveId());
                evaluation.put("title", archive.getArchive_name());
                evaluation.put("type", "interview");
                evaluation.put("createdAt", archive.getCreatedAt());
                evaluation.put("companyName", archive.getCompany() != null ? archive.getCompany().getName() : null);
                evaluation.put("position", archive.getPosition());
                evaluation.put("status", archive.getStatus());
                
                // 평가 점수 조회 및 추가
                InterviewEval eval = interviewEvalRepository.findByInterviewArchiveInterviewArchiveId(archive.getInterviewArchiveId());
                if (eval != null) {
                    evaluation.put("score", eval.getEval_score());
                }
                
                evaluationList.add(evaluation);
            }
            
            // TODO: 자소서 평가, 음성 평가 추가 (필요시)
            
            log.info("사용자 평가 목록 조회 완료: {}", email);
            return ResponseEntity.ok(evaluationList);
        } catch (Exception e) {
            log.error("사용자 평가 목록 조회 실패: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * 특정 평가의 상세 정보를 조회합니다.
     * @param id 평가 ID
     * @return 평가 상세 정보
     */
    @GetMapping("/evaluations/{id}")
    public ResponseEntity<Map<String, Object>> getEvaluationDetail(@PathVariable Integer id) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        
        log.info("평가 상세 정보 조회 요청 - 사용자: {}, 평가 ID: {}", email, id);
        
        try {
            // 인터뷰 아카이브 조회
            Optional<InterviewArchive> archiveOpt = interviewArchiveRepository.findById(id);
            if (archiveOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            
            InterviewArchive archive = archiveOpt.get();
            
            // 사용자 본인의 평가인지 확인
            if (!archive.getUser().getEmail().equals(email)) {
                log.warn("권한 없음: 사용자({})가 다른 사용자의 평가({})에 접근 시도", email, id);
                return ResponseEntity.status(403).build();
            }
            
            // 결과 데이터 구성
            Map<String, Object> result = new HashMap<>();
            result.put("id", archive.getInterviewArchiveId());
            result.put("title", archive.getArchive_name());
            result.put("type", "interview");
            result.put("createdAt", archive.getCreatedAt());
            result.put("companyName", archive.getCompany() != null ? archive.getCompany().getName() : null);
            result.put("position", archive.getPosition());
            
            // 평가 정보 조회 및 추가
            InterviewEval eval = interviewEvalRepository.findByInterviewArchiveInterviewArchiveId(archive.getInterviewArchiveId());
            if (eval != null) {
                result.put("score", eval.getEval_score());
                result.put("comment", eval.getEval_reason());
                
                // 강점 정보
                List<Map<String, String>> goods = new ArrayList<>();
                if (eval.getEval_good_summary() != null && eval.getEval_good_description() != null) {
                    String[] goodSummaries = eval.getEval_good_summary().split("\\|");
                    String[] goodDescriptions = eval.getEval_good_description().split("\\|");
                    
                    for (int i = 0; i < Math.min(goodSummaries.length, goodDescriptions.length); i++) {
                        Map<String, String> good = new HashMap<>();
                        good.put("good_summary", goodSummaries[i]);
                        good.put("good_description", goodDescriptions[i]);
                        goods.add(good);
                    }
                }
                result.put("goods", goods);
                
                // 개선점 정보
                List<Map<String, String>> bads = new ArrayList<>();
                if (eval.getEval_bad_summary() != null && eval.getEval_bad_description() != null) {
                    String[] badSummaries = eval.getEval_bad_summary().split("\\|");
                    String[] badDescriptions = eval.getEval_bad_description().split("\\|");
                    
                    for (int i = 0; i < Math.min(badSummaries.length, badDescriptions.length); i++) {
                        Map<String, String> bad = new HashMap<>();
                        bad.put("bad_summary", badSummaries[i]);
                        bad.put("bad_description", badDescriptions[i]);
                        bads.add(bad);
                    }
                }
                result.put("bads", bads);
                
                // 문제점 및 해결책 정보
                List<Map<String, Object>> problems = new ArrayList<>();
                if (eval.getEval_state() != null && eval.getEval_cause() != null) {
                    String[] states = eval.getEval_state().split("\\|");
                    String[] causes = eval.getEval_cause().split("\\|");
                    
                    for (int i = 0; i < Math.min(states.length, causes.length); i++) {
                        Map<String, Object> problem = new HashMap<>();
                        problem.put("evaluation", states[i]);
                        problem.put("reason", causes[i]);
                        problem.put("title", "면접 답변 문제점");
                        problem.put("description", "면접 답변에서 발견된 문제점입니다");
                        problems.add(problem);
                    }
                }
                result.put("problems", problems);
                
                // 해결책 정보
                List<Map<String, String>> solutions = new ArrayList<>();
                if (eval.getEval_solution() != null) {
                    String[] solutionTexts = eval.getEval_solution().split("\\|");
                    
                    for (String solution : solutionTexts) {
                        Map<String, String> solutionMap = new HashMap<>();
                        solutionMap.put("feedback", solution);
                        solutions.add(solutionMap);
                    }
                }
                result.put("solutions", solutions);
                
                // 개선 방향 정보
                List<Map<String, String>> improvements = new ArrayList<>();
                if (eval.getEval_improvment() != null) {
                    String[] improvementTexts = eval.getEval_improvment().split("\\|");
                    
                    for (String improvement : improvementTexts) {
                        Map<String, String> improvementMap = new HashMap<>();
                        improvementMap.put("improvment", improvement);
                        improvements.add(improvementMap);
                    }
                }
                result.put("improvements", improvements);
            }
            
            // 질문 및 답변 정보 조회 및 추가
            List<InterviewQuestion> questions = interviewQuestionRepository.findAllByInterviewArchive(archive);
            List<InterviewAnswer> answers = interviewAnswerRepository.findAllByInterviewArchive(archive);
            
            List<Map<String, String>> qaList = new ArrayList<>();
            for (InterviewQuestion question : questions) {
                Map<String, String> qa = new HashMap<>();
                qa.put("question", question.getInterview_question());
                qa.put("questionType", question.getInterview_question_type().toString());
                
                // 해당 질문에 대한 답변 찾기
                Optional<InterviewAnswer> answer = answers.stream()
                    .filter(a -> a.getInterviewQuestion().getInterviewQuestionId().equals(question.getInterviewQuestionId()))
                    .findFirst();
                
                qa.put("answer", answer.map(InterviewAnswer::getInterview_answer).orElse(""));
                qaList.add(qa);
            }
            result.put("qaList", qaList);
            
            log.info("평가 상세 정보 조회 완료 - 평가 ID: {}", id);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("평가 상세 정보 조회 실패: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * 특정 평가를 삭제합니다.
     * @param id 평가 ID
     * @return 성공 여부
     */
    @DeleteMapping("/evaluations/{id}")
    public ResponseEntity<Void> deleteEvaluation(@PathVariable Integer id) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        
        log.info("평가 삭제 요청 - 사용자: {}, 평가 ID: {}", email, id);
        
        try {
            interviewService.deleteEval(id, email);

            log.info("평가 삭제 완료 - 평가 ID: {}", id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("평가 삭제 실패: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * 특정 유형의 모든 평가를 삭제합니다.
     * @param type 평가 유형 (interview, resume, voice)
     * @return 성공 여부
     */
    @DeleteMapping("/evaluations")
    public ResponseEntity<Void> deleteAllEvaluations(@RequestParam String type) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        
        log.info("모든 평가 삭제 요청 - 사용자: {}, 유형: {}", email, type);
        
        try {
            if ("interview".equals(type)) {
                // 사용자의 면접 아카이브 목록 조회
                List<InterviewArchive> archives = interviewArchiveRepository.findByUserEmail(email);
                
                // 각 아카이브 및 관련 데이터 삭제
                for (InterviewArchive archive : archives) {
                    interviewEvalRepository.deleteByInterviewArchiveInterviewArchiveId(archive.getInterviewArchiveId());
                    interviewAnswerRepository.deleteAllByInterviewArchive(archive);
                    interviewQuestionRepository.deleteAllByInterviewArchive(archive);
                    interviewArchiveRepository.delete(archive);
                }
                
                log.info("모든 면접 평가 삭제 완료 - 사용자: {}", email);
                return ResponseEntity.ok().build();
            } else {
                // 다른 유형 (자소서, 음성)은 추후 구현
                log.warn("지원되지 않는 평가 유형: {}", type);
                return ResponseEntity.badRequest().build();
            }
        } catch (Exception e) {
            log.error("모든 평가 삭제 실패: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
} 