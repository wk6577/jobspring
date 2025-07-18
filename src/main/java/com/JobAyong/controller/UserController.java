package com.JobAyong.controller;

import com.JobAyong.dto.*;
import com.JobAyong.entity.*;
import com.JobAyong.repository.*;
import com.JobAyong.service.InterviewService;
import com.JobAyong.service.UserService;
import com.JobAyong.constant.ResumeType;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;
import java.util.Base64;

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
    private final ResumeRepository resumeRepository;
    private final ResumeEvalRepository resumeEvalRepository;
    private final UserRepository userRepository;
    private final VoiceRepository voiceRepository;

    @PostMapping("/signup")
    public ResponseEntity<Void> signUp(@RequestBody UserSignUpRequest request) {
        userService.signUp(request);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/check-email")
    public ResponseEntity<Map<String, Object>> checkEmailExists(@RequestParam String email) {
        Map<String, Object> response = new HashMap<>();
        
        // 활성 사용자 중에 존재하는지 확인
        boolean activeExists = userService.existsByEmail(email);
        
        // 탈퇴한 사용자인지 확인
        Optional<User> deletedUser = userRepository.findByEmailAndDeleted(email);
        boolean deletedExists = deletedUser.isPresent();
        
        response.put("exists", activeExists);
        response.put("isDeleted", deletedExists);
        
        if (deletedExists) {
            response.put("message", "탈퇴한 회원입니다. 관리자에게 문의하여 계정 복구를 요청해주세요.");
        } else if (activeExists) {
            response.put("message", "이미 사용 중인 이메일입니다.");
        } else {
            response.put("message", "사용 가능한 이메일입니다.");
        }
        
        return ResponseEntity.ok(response);
    }

    /**
     * 현재 로그인한 사용자의 상태 확인
     */
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getCurrentUserStatus() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String email = authentication.getName();
            
            User user = userRepository.findByEmailAndNotDeleted(email)
                    .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", user.getStatus());
            response.put("role", user.getUserRole().getValue());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("사용자 상태 조회 실패: {}", e.getMessage());
            return ResponseEntity.status(500).build();
        }
    }

    @GetMapping("/{email}")
    public ResponseEntity<UserInfoResponse> getUserByEmail(@PathVariable String email) {
        try {
            log.info("사용자 정보 조회 요청: {}", email);
            User user = userService.findByEmail(email);
            log.info("조회된 사용자 정보 - 이름: {}, 생년월일: {}, 전화번호: {}, 성별: {}", 
                user.getName(), user.getBirth(), user.getPhoneNumber(), user.getGender());

            // 프로필 이미지를 Base64로 변환
            String imageUrl = null;
            if (user.getProfileImage() != null && user.getProfileImage().length > 0) {
                String base64Image = Base64.getEncoder().encodeToString(user.getProfileImage());
                // MIME 타입은 기본적으로 image/jpeg로 설정 (실제로는 original_filename에서 확장자를 추출하여 설정 가능)
                imageUrl = "data:image/jpeg;base64," + base64Image;
            }
            
            UserInfoResponse response = new UserInfoResponse(
                user.getEmail(),
                user.getName(),
                user.getBirth() != null ? user.getBirth().toString() : null,
                user.getPhoneNumber(),
                user.getGender() != null ? user.getGender().toString() : null,
                imageUrl, // 사용자 프로필 Base64 이미지 URL
                user.getJob(), // 직무 정보
                user.getCompany(), // 회사 정보
                user.getUserRole(),
                user.getStatus() != null ? user.getStatus() : "ACTIVE"
            );
            
            log.info("응답 데이터 - 생년월일: {}, 전화번호: {}, 성별: {}", 
                response.getBirth(), response.getPhoneNumber(), response.getGender());
            
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            log.error("사용자 정보 조회 실패: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    /*@apiNote 사용자의 직무 회사를 수정하는 API
    * @author 나세호
    * */
    @PutMapping("/jobcom")
    public ResponseEntity<UserUpdateResponse> updateUser(@RequestBody UserUpdateRequest request) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            User updatedUser = userService.whoareyou(authentication.getName());

            return ResponseEntity.ok(userService.updateUser(updatedUser, request));
        } catch (RuntimeException e) {
            log.error("사용자 정보 수정 실패: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (JsonProcessingException e) {
            log.error("사용자 정보 Json 파싱 실패: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }



    // ********************************************************************************************
    // 마이페이지 회원 정보 수정
    // ********************************************************************************************
    @PutMapping("/{email}")
    public ResponseEntity<UserInfoResponse> updateUser(@PathVariable String email, @RequestBody UserProfileUpdateRequest request) {
        try {
            log.info("사용자 정보 수정 요청: {}", email);
            User updatedUser = userService.updateUser(email, request);

            // 프로필 이미지를 Base64로 변환
            String imageUrl = null;
            if (updatedUser.getProfileImage() != null && updatedUser.getProfileImage().length > 0) {
                String base64Image = Base64.getEncoder().encodeToString(updatedUser.getProfileImage());
                imageUrl = "data:image/jpeg;base64," + base64Image;
            }

            UserInfoResponse response = new UserInfoResponse(
                    updatedUser.getEmail(),
                    updatedUser.getName(),
                    updatedUser.getBirth() != null ? updatedUser.getBirth().toString() : null,
                    updatedUser.getPhoneNumber(),
                    updatedUser.getGender() != null ? updatedUser.getGender().toString() : null,
                    imageUrl, // 프로필 이미지 Base64 URL
                    updatedUser.getJob(), // 직무 정보
                    updatedUser.getCompany(), // 회사 정보
                    updatedUser.getUserRole(),
                    updatedUser.getStatus() != null ? updatedUser.getStatus() : "ACTIVE"
            );

            log.info("사용자 정보 수정 완료: {}", email);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            log.error("사용자 정보 수정 실패: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }
    // ********************************************************************************************


    // ********************************************************************************************
    // 마이페이지 프로필 이미지 수정
    // ********************************************************************************************
    @PostMapping("/profile-image")
    public ResponseEntity<ProfileImageUploadResponse> uploadProfileImage(@RequestParam("email") String email,
                                                                         @RequestParam("file") MultipartFile file)
    {
        log.info("사용자 정보 수정 요청: {}", email);
        ProfileImageUploadResponse response =  userService.updateProfileImage(email, file);

        log.info("사용자 프로필 정보 수정 완료: {}", email);
        return ResponseEntity.ok(response);
    }
    // ********************************************************************************************



    @PutMapping("/password")
    public ResponseEntity<Void> changePassword(@RequestBody PasswordChangeRequest request) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String email = authentication.getName();

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
     * 회원탈퇴 - soft delete 방식
     */
    @PutMapping("/withdraw")
    public ResponseEntity<Void> withdrawUser() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String email = authentication.getName();
            
            log.info("회원탈퇴 요청: {}", email);
            userService.withdrawUser(email);
            log.info("회원탈퇴 성공: {}", email);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            log.error("회원탈퇴 실패: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 관리자용 - 탈퇴한 회원 복구
     */
    @PutMapping("/restore/{email}")
    public ResponseEntity<Void> restoreUser(@PathVariable String email) {
        try {
            log.info("회원 복구 요청: {}", email);
            
            // 대상 사용자가 관리자인지 확인
            User targetUser = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("대상 사용자를 찾을 수 없습니다."));
            
            if (targetUser.getUserRole().getValue().equals("admin")) {
                log.warn("관리자 계정의 복구 시도: {}", email);
                return ResponseEntity.status(403).build();
            }
            
            userService.restoreUser(email);
            log.info("회원 복구 성공: {}", email);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            log.error("회원 복구 실패: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 관리자용 - 모든 사용자 목록 조회 (탈퇴한 사용자 포함)
     */
    @GetMapping("/admin/users")
    public ResponseEntity<List<Map<String, Object>>> getAllUsers() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String email = authentication.getName();
            
            log.info("관리자 사용자 목록 조회 요청: {}", email);
            
            // 관리자 권한 확인
            User admin = userRepository.findByEmailAndNotDeleted(email)
                    .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
            
            log.info("사용자 역할 확인: {}", admin.getUserRole().getValue());
            
            if (!admin.getUserRole().getValue().equals("admin")) {
                log.warn("관리자 권한 없는 사용자의 접근 시도: {} (역할: {})", email, admin.getUserRole().getValue());
                return ResponseEntity.status(403).build();
            }
            
            List<Map<String, Object>> userList = userService.getAllUsersForAdmin();
            log.info("사용자 목록 조회 성공: {} 명의 사용자 조회", userList.size());
            return ResponseEntity.ok(userList);
        } catch (Exception e) {
            log.error("사용자 목록 조회 실패: {}", e.getMessage(), e);
            return ResponseEntity.status(500).build();
        }
    }

    /**
     * 관리자용 - 사용자 상태 변경 (활성/정지)
     */
    @PutMapping("/admin/users/{email}/status")
    public ResponseEntity<Void> updateUserStatus(@PathVariable String email, @RequestBody Map<String, String> request) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String adminEmail = authentication.getName();
            
            log.info("사용자 상태 변경 요청 - 관리자: {}, 대상 사용자: {}", adminEmail, email);
            
            // 관리자 권한 확인
            User admin = userRepository.findByEmailAndNotDeleted(adminEmail)
                    .orElseThrow(() -> new RuntimeException("관리자를 찾을 수 없습니다."));
            
            if (!admin.getUserRole().getValue().equals("admin")) {
                log.warn("관리자 권한 없는 사용자의 상태 변경 시도: {} (역할: {})", adminEmail, admin.getUserRole().getValue());
                return ResponseEntity.status(403).build();
            }
            
            String newStatus = request.get("status");
            if (newStatus == null || (!newStatus.equals("ACTIVE") && !newStatus.equals("SUSPENDED"))) {
                log.warn("잘못된 상태 값: {}", newStatus);
                return ResponseEntity.badRequest().build();
            }
            
            // 대상 사용자가 관리자인지 확인
            User targetUser = userRepository.findByEmailAndNotDeleted(email)
                    .orElseThrow(() -> new RuntimeException("대상 사용자를 찾을 수 없습니다."));
            
            if (targetUser.getUserRole().getValue().equals("admin")) {
                log.warn("관리자 계정의 상태 변경 시도: {}", email);
                return ResponseEntity.status(403).build();
            }
            
            userService.updateUserStatus(email, newStatus);
            log.info("사용자 상태 변경 성공: {} -> {}", email, newStatus);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("사용자 상태 변경 실패: {}", e.getMessage(), e);
            return ResponseEntity.status(500).build();
        }
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
            List<InterviewArchive> interviewArchives = interviewArchiveRepository.findByUserEmailAndDeletedAtIsNull(email);
            
            // 최신순으로 정렬 (createdAt 기준 내림차순)
            interviewArchives.sort((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()));
            
            for (InterviewArchive archive : interviewArchives) {
                Map<String, Object> evaluation = new HashMap<>();
                evaluation.put("id", archive.getInterviewArchiveId());
                evaluation.put("title", archive.getArchive_name());
                evaluation.put("type", "interview");
                evaluation.put("createdAt", archive.getCreatedAt());
                evaluation.put("companyName", archive.getCompany() != null ? archive.getCompany() : null);
                evaluation.put("position", archive.getPosition());
                evaluation.put("status", archive.getStatus());
                evaluation.put("archive_mode", archive.getMode().toString()); // archive_mode 추가
                
                // 평가 점수 조회 및 추가
                InterviewEval eval = interviewEvalRepository.findByInterviewArchiveInterviewArchiveId(archive.getInterviewArchiveId());
                if (eval != null) {
                    evaluation.put("score", eval.getEval_score());
                }
                
                evaluationList.add(evaluation);
            }
            
            // 자소서 평가 목록 조회 (각 자소서별 최신 버전만)
            List<ResumeEval> resumeEvals = resumeEvalRepository.findLatestVersionByUserEmailAndDeletedAtIsNull(email);
            
            for (ResumeEval resumeEval : resumeEvals) {
                Map<String, Object> evaluation = new HashMap<>();
                evaluation.put("id", resumeEval.getResumeEvalId());
                evaluation.put("title", resumeEval.getResumeEvalTitle());
                evaluation.put("type", "resume");
                evaluation.put("createdAt", resumeEval.getCreatedAt());
                evaluation.put("companyName", null); // Resume 엔티티에 회사명 필드가 없음
                evaluation.put("position", null); // Resume 엔티티에 포지션 필드가 없음
                evaluation.put("status", "DONE"); // 자소서 평가는 완료된 상태로 저장됨
                evaluation.put("score", null); // ResumeEval 엔티티에 점수 필드가 없음
                evaluation.put("version", resumeEval.getResumeEvalVersion()); // 자소서 평가 버전 추가
                
                evaluationList.add(evaluation);
            }
            
            // 전체 목록을 생성일 기준으로 내림차순 정렬
            evaluationList.sort((a, b) -> {
                Object aCreatedAt = a.get("createdAt");
                Object bCreatedAt = b.get("createdAt");
                if (aCreatedAt instanceof java.time.LocalDateTime && bCreatedAt instanceof java.time.LocalDateTime) {
                    return ((java.time.LocalDateTime) bCreatedAt).compareTo((java.time.LocalDateTime) aCreatedAt);
                }
                return 0;
            });
            
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
     * @param type 평가 타입 (선택적 파라미터)
     * @return 평가 상세 정보
     */
    @GetMapping("/evaluations/{id}")
    public ResponseEntity<Map<String, Object>> getEvaluationDetail(@PathVariable Integer id, @RequestParam(required = false) String type) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        
        log.info("평가 상세 정보 조회 요청 - 사용자: {}, 평가 ID: {}, 타입: {}", email, id, type);
        
        try {
            // 타입이 명시적으로 지정된 경우 해당 타입에서만 조회
            if ("resume".equals(type)) {
                Optional<ResumeEval> resumeEvalOpt = resumeEvalRepository.findById(id);
                if (resumeEvalOpt.isPresent()) {
                    ResumeEval resumeEval = resumeEvalOpt.get();
                    
                    // 사용자 본인의 평가인지 확인
                    if (!resumeEval.getUser().getEmail().equals(email)) {
                        log.warn("권한 없음: 사용자({})가 다른 사용자의 자소서 평가({})에 접근 시도", email, id);
                        return ResponseEntity.status(403).build();
                    }
                    
                    return getResumeEvaluationDetail(resumeEval);
                }
                return ResponseEntity.notFound().build();
            } else if ("interview".equals(type)) {
                Optional<InterviewArchive> archiveOpt = interviewArchiveRepository.findById(id);
                if (archiveOpt.isPresent()) {
                    InterviewArchive archive = archiveOpt.get();
                    
                    // 사용자 본인의 평가인지 확인
                    if (!archive.getUser().getEmail().equals(email)) {
                        log.warn("권한 없음: 사용자({})가 다른 사용자의 면접 평가({})에 접근 시도", email, id);
                        return ResponseEntity.status(403).build();
                    }
                    
                    return getInterviewEvaluationDetail(archive);
                }
                return ResponseEntity.notFound().build();
            }
            
            // 타입이 지정되지 않은 경우 자소서 평가부터 확인
            Optional<ResumeEval> resumeEvalOpt = resumeEvalRepository.findById(id);
            if (resumeEvalOpt.isPresent()) {
                ResumeEval resumeEval = resumeEvalOpt.get();
                
                // 사용자 본인의 평가인지 확인
                if (!resumeEval.getUser().getEmail().equals(email)) {
                    log.warn("권한 없음: 사용자({})가 다른 사용자의 자소서 평가({})에 접근 시도", email, id);
                    return ResponseEntity.status(403).build();
                }
                
                return getResumeEvaluationDetail(resumeEval);
            }
            
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("평가 상세 정보 조회 실패: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 특정 자소서 평가의 모든 버전을 조회합니다.
     * @param id 자소서 평가 ID
     * @return 해당 자소서의 모든 버전 목록
     */
    @GetMapping("/evaluations/{id}/versions")
    public ResponseEntity<List<Map<String, Object>>> getResumeEvaluationVersions(@PathVariable Integer id) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        
        log.info("자소서 평가 버전 목록 조회 요청 - 사용자: {}, 평가 ID: {}", email, id);
        
        try {
            // 먼저 해당 평가가 존재하고 사용자 본인의 것인지 확인
            Optional<ResumeEval> resumeEvalOpt = resumeEvalRepository.findById(id);
            if (!resumeEvalOpt.isPresent()) {
                return ResponseEntity.notFound().build();
            }
            
            ResumeEval resumeEval = resumeEvalOpt.get();
            
            // 사용자 본인의 평가인지 확인
            if (!resumeEval.getUser().getEmail().equals(email)) {
                log.warn("권한 없음: 사용자({})가 다른 사용자의 자소서 평가({})에 접근 시도", email, id);
                return ResponseEntity.status(403).build();
            }
            
            // 해당 자소서의 모든 버전 조회 (resume_id가 NULL인 경우 해당 평가만 반환)
            List<ResumeEval> allVersions = new ArrayList<>();
            Integer resumeId = null;
            
            if (resumeEval.getResume() != null) {
                resumeId = resumeEval.getResume().getResumeId();
                allVersions = resumeEvalRepository.findAllVersionsByResumeId(resumeId);
            } else {
                // resume_id가 NULL인 경우 해당 평가만 추가 (자기소개서가 삭제된 평가)
                allVersions.add(resumeEval);
            }
            
            // 버전 목록 구성
            List<Map<String, Object>> versionList = new ArrayList<>();
            for (ResumeEval version : allVersions) {
                Map<String, Object> versionInfo = new HashMap<>();
                versionInfo.put("id", version.getResumeEvalId());
                versionInfo.put("version", version.getResumeEvalVersion());
                versionInfo.put("createdAt", version.getCreatedAt());
                versionList.add(versionInfo);
            }
            
            log.info("자소서 평가 버전 목록 조회 완료 - 자소서 ID: {}, 버전 수: {}", resumeId, versionList.size());
            return ResponseEntity.ok(versionList);
        } catch (Exception e) {
            log.error("자소서 평가 버전 목록 조회 실패: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * 면접 평가 상세 정보를 구성합니다.
     */
    private ResponseEntity<Map<String, Object>> getInterviewEvaluationDetail(InterviewArchive archive) {
        try {
            
            // 결과 데이터 구성
            Map<String, Object> result = new HashMap<>();
            result.put("id", archive.getInterviewArchiveId());
            result.put("title", archive.getArchive_name());
            result.put("type", "interview");
            result.put("createdAt", archive.getCreatedAt());
            result.put("companyName", archive.getCompany() != null ? archive.getCompany() : null);
            result.put("position", archive.getPosition());
            result.put("archive_mode", archive.getMode().toString()); // archive_mode 추가

            
            // 평가 정보 조회 및 추가
            InterviewEval eval = interviewEvalRepository.findByInterviewArchiveInterviewArchiveId(archive.getInterviewArchiveId());
            if (eval != null) {
                result.put("score", eval.getEval_score());
                result.put("comment", eval.getEval_reason());
                result.put("prev_summary", eval.getPrev_summary());
                result.put("prev_description", eval.getPrev_description());
                
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
            
            log.info("면접 평가 상세 정보 조회 완료 - 평가 ID: {}", archive.getInterviewArchiveId());
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("면접 평가 상세 정보 조회 실패: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * 자소서 평가 상세 정보를 구성합니다.
     */
    private ResponseEntity<Map<String, Object>> getResumeEvaluationDetail(ResumeEval resumeEval) {
        try {
            // 결과 데이터 구성
            Map<String, Object> result = new HashMap<>();
            result.put("id", resumeEval.getResumeEvalId());
            result.put("title", resumeEval.getResumeEvalTitle());
            result.put("type", "resume");
            result.put("createdAt", resumeEval.getCreatedAt());
            result.put("companyName", null); // Resume 엔티티에 회사명 필드가 없음
            result.put("position", null); // Resume 엔티티에 포지션 필드가 없음
            result.put("score", null); // ResumeEval 엔티티에 점수 필드가 없음
            
            // 자소서 평가 정보
            result.put("originalResume", resumeEval.getResumeOrg());
            result.put("improvedResume", resumeEval.getResumeImp());
            result.put("finalResume", resumeEval.getResumeFin());
            result.put("reason", resumeEval.getReason());
            result.put("missingAreas", resumeEval.getMissingAreas());
            result.put("version", resumeEval.getResumeEvalVersion());
            
            log.info("자소서 평가 상세 정보 조회 완료 - 평가 ID: {}", resumeEval.getResumeEvalId());
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("자소서 평가 상세 정보 조회 실패: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * 특정 평가를 휴지통으로 이동합니다 (소프트 삭제).
     * @param id 평가 ID
     * @param type 평가 타입 (선택적 파라미터)
     * @return 성공 여부
     */
    @PutMapping("/evaluations/{id}/soft-delete")
    public ResponseEntity<Void> softDeleteEvaluation(@PathVariable Integer id, @RequestParam(required = false) String type) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        
        log.info("평가 휴지통 이동 요청 - 사용자: {}, 평가 ID: {}, 타입: {}", email, id, type);
        
        try {
            // 타입이 명시적으로 지정된 경우 해당 타입에서만 조회
            if ("resume".equals(type)) {
                Optional<ResumeEval> resumeEvalOpt = resumeEvalRepository.findById(id);
                if (resumeEvalOpt.isPresent()) {
                    ResumeEval resumeEval = resumeEvalOpt.get();
                    
                    // 사용자 본인의 평가인지 확인
                    if (!resumeEval.getUser().getEmail().equals(email)) {
                        log.warn("권한 없음: 사용자({})가 다른 사용자의 자소서 평가({})에 접근 시도", email, id);
                        return ResponseEntity.status(403).build();
                    }
                    
                    // 같은 resume_id의 모든 버전을 소프트 삭제
                    Integer resumeId = resumeEval.getResume().getResumeId();
                    java.time.LocalDateTime now = java.time.LocalDateTime.now();
                    resumeEvalRepository.softDeleteAllVersionsByResumeId(resumeId, now);

                    log.info("자소서 평가 모든 버전 휴지통 이동 완료 - 자소서 ID: {}, 평가 ID: {}", resumeId, id);
                    return ResponseEntity.ok().build();
                }
                return ResponseEntity.notFound().build();
            } else if ("interview".equals(type)) {
                Optional<InterviewArchive> archiveOpt = interviewArchiveRepository.findById(id);
                if (archiveOpt.isPresent()) {
                    InterviewArchive archive = archiveOpt.get();
                    
                    // 사용자 본인의 평가인지 확인
                    if (!archive.getUser().getEmail().equals(email)) {
                        log.warn("권한 없음: 사용자({})가 다른 사용자의 평가({})에 접근 시도", email, id);
                        return ResponseEntity.status(403).build();
                    }
                    
                    // 소프트 삭제 (deleted_at에 현재 시간 설정)
                    archive.setDeletedAt(java.time.LocalDateTime.now());
                    interviewArchiveRepository.save(archive);

                    log.info("면접 평가 휴지통 이동 완료 - 평가 ID: {}", id);
                    return ResponseEntity.ok().build();
                }
                return ResponseEntity.notFound().build();
            }
            
            // 타입이 지정되지 않은 경우 기존 로직 (하위 호환성)
            // 먼저 면접 평가에서 조회
            Optional<InterviewArchive> archiveOpt = interviewArchiveRepository.findById(id);
            if (archiveOpt.isPresent()) {
                InterviewArchive archive = archiveOpt.get();
                
                // 사용자 본인의 평가인지 확인
                if (!archive.getUser().getEmail().equals(email)) {
                    log.warn("권한 없음: 사용자({})가 다른 사용자의 평가({})에 접근 시도", email, id);
                    return ResponseEntity.status(403).build();
                }
                
                // 소프트 삭제 (deleted_at에 현재 시간 설정)
                archive.setDeletedAt(java.time.LocalDateTime.now());
                interviewArchiveRepository.save(archive);

                log.info("면접 평가 휴지통 이동 완료 - 평가 ID: {}", id);
                return ResponseEntity.ok().build();
            }
            
            // 면접 평가가 없으면 자소서 평가에서 조회
            Optional<ResumeEval> resumeEvalOpt = resumeEvalRepository.findById(id);
            if (resumeEvalOpt.isPresent()) {
                ResumeEval resumeEval = resumeEvalOpt.get();
                
                // 사용자 본인의 평가인지 확인
                if (!resumeEval.getUser().getEmail().equals(email)) {
                    log.warn("권한 없음: 사용자({})가 다른 사용자의 자소서 평가({})에 접근 시도", email, id);
                    return ResponseEntity.status(403).build();
                }
                
                // 같은 resume_id의 모든 버전을 소프트 삭제
                Integer resumeId = resumeEval.getResume().getResumeId();
                java.time.LocalDateTime now = java.time.LocalDateTime.now();
                resumeEvalRepository.softDeleteAllVersionsByResumeId(resumeId, now);

                log.info("자소서 평가 모든 버전 휴지통 이동 완료 - 자소서 ID: {}, 평가 ID: {}", resumeId, id);
                return ResponseEntity.ok().build();
            }
            
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("평가 휴지통 이동 실패: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 휴지통에 있는 평가 목록을 조회합니다.
     * @return 휴지통 평가 목록
     */
    @GetMapping("/trash")
    public ResponseEntity<List<Map<String, Object>>> getTrashList() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        
        log.info("휴지통 목록 조회 요청 - 사용자: {}", email);
        
        try {
            List<Map<String, Object>> trashList = new ArrayList<>();
            List<InterviewArchive> deletedArchives = interviewArchiveRepository.findByUserEmailAndDeletedAtIsNotNull(email);
            
            // 최신순으로 정렬 (deletedAt 기준 내림차순, deletedAt이 null이면 createdAt 기준)
            deletedArchives.sort((a, b) -> {
                if (a.getDeletedAt() != null && b.getDeletedAt() != null) {
                    return b.getDeletedAt().compareTo(a.getDeletedAt());
                } else {
                    return b.getCreatedAt().compareTo(a.getCreatedAt());
                }
            });
            
            for (InterviewArchive archive : deletedArchives) {
                Map<String, Object> evaluation = new HashMap<>();
                evaluation.put("id", archive.getInterviewArchiveId());
                evaluation.put("title", archive.getArchive_name());
                evaluation.put("type", "interview");
                evaluation.put("createdAt", archive.getCreatedAt());
                evaluation.put("deletedAt", archive.getDeletedAt());
                evaluation.put("companyName", archive.getCompany() != null ? archive.getCompany() : null);
                evaluation.put("position", archive.getPosition());
                evaluation.put("status", archive.getStatus());
                evaluation.put("archive_mode", archive.getMode().toString());
                
                // 평가 점수 조회 및 추가
                InterviewEval eval = interviewEvalRepository.findByInterviewArchiveInterviewArchiveId(archive.getInterviewArchiveId());
                if (eval != null) {
                    evaluation.put("score", eval.getEval_score());
                }
                
                trashList.add(evaluation);
            }
            
            // 삭제된 자소서 평가 목록 조회 (각 자소서별 최신 버전만)
            List<ResumeEval> deletedResumeEvals = resumeEvalRepository.findLatestVersionByUserEmailAndDeletedAtIsNotNull(email);
            
            for (ResumeEval resumeEval : deletedResumeEvals) {
                Map<String, Object> evaluation = new HashMap<>();
                evaluation.put("id", resumeEval.getResumeEvalId());
                evaluation.put("title", resumeEval.getResumeEvalTitle());
                evaluation.put("type", "resume");
                evaluation.put("createdAt", resumeEval.getCreatedAt());
                evaluation.put("deletedAt", resumeEval.getDeletedAt());
                evaluation.put("companyName", null); // Resume 엔티티에 회사명 필드가 없음
                evaluation.put("position", null); // Resume 엔티티에 포지션 필드가 없음
                evaluation.put("status", "DONE");
                evaluation.put("score", null); // ResumeEval 엔티티에 점수 필드가 없음
                evaluation.put("version", resumeEval.getResumeEvalVersion()); // 자소서 평가 버전 추가
                
                trashList.add(evaluation);
            }
            
            // 전체 목록을 삭제일 기준으로 내림차순 정렬
            trashList.sort((a, b) -> {
                Object aDeletedAt = a.get("deletedAt");
                Object bDeletedAt = b.get("deletedAt");
                if (aDeletedAt instanceof java.time.LocalDateTime && bDeletedAt instanceof java.time.LocalDateTime) {
                    return ((java.time.LocalDateTime) bDeletedAt).compareTo((java.time.LocalDateTime) aDeletedAt);
                }
                return 0;
            });
            
            log.info("휴지통 목록 조회 완료 - 사용자: {}, 항목 수: {}", email, trashList.size());
            return ResponseEntity.ok(trashList);
        } catch (Exception e) {
            log.error("휴지통 목록 조회 실패: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 휴지통에서 평가를 복구합니다.
     * @param id 평가 ID
     * @param type 평가 타입 (선택적 파라미터)
     * @return 성공 여부
     */
    @PutMapping("/evaluations/{id}/restore")
    public ResponseEntity<Void> restoreEvaluation(@PathVariable Integer id, @RequestParam(required = false) String type) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        
        log.info("평가 복구 요청 - 사용자: {}, 평가 ID: {}, 타입: {}", email, id, type);
        
        try {
            // 타입이 명시적으로 지정된 경우 해당 타입에서만 조회
            if ("resume".equals(type)) {
                Optional<ResumeEval> resumeEvalOpt = resumeEvalRepository.findById(id);
                if (resumeEvalOpt.isPresent()) {
                    ResumeEval resumeEval = resumeEvalOpt.get();
                    
                    // 사용자 본인의 평가인지 확인
                    if (!resumeEval.getUser().getEmail().equals(email)) {
                        log.warn("권한 없음: 사용자({})가 다른 사용자의 자소서 평가({})에 접근 시도", email, id);
                        return ResponseEntity.status(403).build();
                    }
                    
                    // 같은 resume_id의 모든 버전을 복구 (resume_id가 NULL인 경우 해당 평가만 복구)
                    if (resumeEval.getResume() != null) {
                        Integer resumeId = resumeEval.getResume().getResumeId();
                        resumeEvalRepository.restoreAllVersionsByResumeId(resumeId);
                        log.info("자소서 평가 모든 버전 복구 완료 - 자소서 ID: {}, 평가 ID: {}", resumeId, id);
                    } else {
                        // resume_id가 NULL인 경우 해당 평가만 복구
                        resumeEval.setDeletedAt(null);
                        resumeEvalRepository.save(resumeEval);
                        log.info("자소서 평가 복구 완료 (자소서 삭제됨) - 평가 ID: {}", id);
                    }
                    return ResponseEntity.ok().build();
                }
                return ResponseEntity.notFound().build();
            } else if ("interview".equals(type)) {
                Optional<InterviewArchive> archiveOpt = interviewArchiveRepository.findById(id);
                if (archiveOpt.isPresent()) {
                    InterviewArchive archive = archiveOpt.get();
                    
                    // 사용자 본인의 평가인지 확인
                    if (!archive.getUser().getEmail().equals(email)) {
                        log.warn("권한 없음: 사용자({})가 다른 사용자의 평가({})에 접근 시도", email, id);
                        return ResponseEntity.status(403).build();
                    }
                    
                    // 복구 (deleted_at을 null로 설정하고 updated_at 갱신)
                    archive.setDeletedAt(null);
                    archive.setUpdatedAt(java.time.LocalDateTime.now());
                    interviewArchiveRepository.save(archive);

                    log.info("면접 평가 복구 완료 - 평가 ID: {}", id);
                    return ResponseEntity.ok().build();
                }
                return ResponseEntity.notFound().build();
            }
            
            // 타입이 지정되지 않은 경우 기존 로직 (하위 호환성)
            // 먼저 면접 평가에서 조회
            Optional<InterviewArchive> archiveOpt = interviewArchiveRepository.findById(id);
            if (archiveOpt.isPresent()) {
                InterviewArchive archive = archiveOpt.get();
                
                // 사용자 본인의 평가인지 확인
                if (!archive.getUser().getEmail().equals(email)) {
                    log.warn("권한 없음: 사용자({})가 다른 사용자의 평가({})에 접근 시도", email, id);
                    return ResponseEntity.status(403).build();
                }
                
                // 복구 (deleted_at을 null로 설정하고 updated_at 갱신)
                archive.setDeletedAt(null);
                archive.setUpdatedAt(java.time.LocalDateTime.now());
                interviewArchiveRepository.save(archive);

                log.info("면접 평가 복구 완료 - 평가 ID: {}", id);
                return ResponseEntity.ok().build();
            }
            
            // 면접 평가가 없으면 자소서 평가에서 조회
            Optional<ResumeEval> resumeEvalOpt = resumeEvalRepository.findById(id);
            if (resumeEvalOpt.isPresent()) {
                ResumeEval resumeEval = resumeEvalOpt.get();
                
                // 사용자 본인의 평가인지 확인
                if (!resumeEval.getUser().getEmail().equals(email)) {
                    log.warn("권한 없음: 사용자({})가 다른 사용자의 자소서 평가({})에 접근 시도", email, id);
                    return ResponseEntity.status(403).build();
                }
                
                // 같은 resume_id의 모든 버전을 복구 (resume_id가 NULL인 경우 해당 평가만 복구)
                if (resumeEval.getResume() != null) {
                    Integer resumeId = resumeEval.getResume().getResumeId();
                    resumeEvalRepository.restoreAllVersionsByResumeId(resumeId);
                    log.info("자소서 평가 모든 버전 복구 완료 - 자소서 ID: {}, 평가 ID: {}", resumeId, id);
                } else {
                    // resume_id가 NULL인 경우 해당 평가만 복구
                    resumeEval.setDeletedAt(null);
                    resumeEvalRepository.save(resumeEval);
                    log.info("자소서 평가 복구 완료 (자소서 삭제됨) - 평가 ID: {}", id);
                }
                return ResponseEntity.ok().build();
            }
            
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("평가 복구 실패: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 특정 평가를 완전히 삭제합니다 (하드 삭제).
     * @param id 평가 ID
     * @param type 평가 타입 (선택적 파라미터)
     * @return 성공 여부
     */
    @DeleteMapping("/evaluations/{id}")
    public ResponseEntity<Void> deleteEvaluation(@PathVariable Integer id, @RequestParam(required = false) String type) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        
        log.info("평가 완전 삭제 요청 - 사용자: {}, 평가 ID: {}, 타입: {}", email, id, type);
        
        try {
            // 타입이 명시적으로 지정된 경우 해당 타입에서만 조회
            if ("resume".equals(type)) {
                Optional<ResumeEval> resumeEvalOpt = resumeEvalRepository.findById(id);
                if (resumeEvalOpt.isPresent()) {
                    ResumeEval resumeEval = resumeEvalOpt.get();
                    
                    // 사용자 본인의 평가인지 확인
                    if (!resumeEval.getUser().getEmail().equals(email)) {
                        log.warn("권한 없음: 사용자({})가 다른 사용자의 자소서 평가({})에 접근 시도", email, id);
                        return ResponseEntity.status(403).build();
                    }
                    
                    // 같은 resume_id의 모든 버전을 완전 삭제 (resume_id가 NULL인 경우 해당 평가만 삭제)
                    if (resumeEval.getResume() != null) {
                        Integer resumeId = resumeEval.getResume().getResumeId();
                        resumeEvalRepository.deleteAllVersionsByResumeId(resumeId);
                        log.info("자소서 평가 모든 버전 완전 삭제 완료 - 자소서 ID: {}, 평가 ID: {}", resumeId, id);
                    } else {
                        // resume_id가 NULL인 경우 해당 평가만 삭제
                        resumeEvalRepository.delete(resumeEval);
                        log.info("자소서 평가 완전 삭제 완료 (자소서 삭제됨) - 평가 ID: {}", id);
                    }
                    return ResponseEntity.ok().build();
                }
                return ResponseEntity.notFound().build();
            } else if ("interview".equals(type)) {
                interviewService.deleteEval(id, email);
                log.info("면접 평가 완전 삭제 완료 - 평가 ID: {}", id);
                return ResponseEntity.ok().build();
            }
            
            // 타입이 지정되지 않은 경우 기존 로직 (하위 호환성)
            // 먼저 자소서 평가에서 조회
            Optional<ResumeEval> resumeEvalOpt = resumeEvalRepository.findById(id);
            if (resumeEvalOpt.isPresent()) {
                ResumeEval resumeEval = resumeEvalOpt.get();
                
                // 사용자 본인의 평가인지 확인
                if (!resumeEval.getUser().getEmail().equals(email)) {
                    log.warn("권한 없음: 사용자({})가 다른 사용자의 자소서 평가({})에 접근 시도", email, id);
                    return ResponseEntity.status(403).build();
                }
                
                // 같은 resume_id의 모든 버전을 완전 삭제 (resume_id가 NULL인 경우 해당 평가만 삭제)
                if (resumeEval.getResume() != null) {
                    Integer resumeId = resumeEval.getResume().getResumeId();
                    resumeEvalRepository.deleteAllVersionsByResumeId(resumeId);
                    log.info("자소서 평가 모든 버전 완전 삭제 완료 - 자소서 ID: {}, 평가 ID: {}", resumeId, id);
                } else {
                    // resume_id가 NULL인 경우 해당 평가만 삭제
                    resumeEvalRepository.delete(resumeEval);
                    log.info("자소서 평가 완전 삭제 완료 (자소서 삭제됨) - 평가 ID: {}", id);
                }
                return ResponseEntity.ok().build();
            }
            
            // 자소서 평가가 없으면 면접 평가로 처리
            interviewService.deleteEval(id, email);
            log.info("면접 평가 완전 삭제 완료 - 평가 ID: {}", id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("평가 완전 삭제 실패: {}", e.getMessage());
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

    /**
     * 사용자의 자기소개서 목록을 조회합니다.
     * @return 자기소개서 목록
     */
    @GetMapping("/resume")
    public ResponseEntity<List<Map<String, Object>>> getResumeList() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        
        log.info("자기소개서 목록 조회 요청 - 사용자: {}", email);
        
        try {
            // 삭제되지 않은 자기소개서만 조회
            List<Resume> resumes = resumeRepository.findActiveResumesByUserEmail(email);
            
            // 최신순으로 정렬 (updatedAt 기준 내림차순, updatedAt이 null이면 createdAt 기준)
            resumes.sort((a, b) -> {
                if (a.getUpdatedAt() != null && b.getUpdatedAt() != null) {
                    return b.getUpdatedAt().compareTo(a.getUpdatedAt());
                } else if (a.getUpdatedAt() == null && b.getUpdatedAt() == null) {
                    return b.getCreatedAt().compareTo(a.getCreatedAt());
                } else if (a.getUpdatedAt() == null) {
                    return b.getUpdatedAt().compareTo(a.getCreatedAt());
                } else {
                    return b.getCreatedAt().compareTo(a.getUpdatedAt());
                }
            });
            
            List<Map<String, Object>> resumeList = new ArrayList<>();
            
            for (Resume resume : resumes) {
                Map<String, Object> resumeData = new HashMap<>();
                resumeData.put("id", resume.getResumeId());
                resumeData.put("title", resume.getResumeTitle());
                resumeData.put("resumeText", resume.getResumeText());
                resumeData.put("createdAt", resume.getCreatedAt());
                resumeData.put("updatedAt", resume.getUpdatedAt());
                resumeList.add(resumeData);
            }
            
            log.info("자기소개서 목록 조회 완료 - 사용자: {}, 항목 수: {}", email, resumeList.size());
            return ResponseEntity.ok(resumeList);
        } catch (Exception e) {
            log.error("자기소개서 목록 조회 실패: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 특정 자기소개서를 조회합니다.
     * @param id 자기소개서 ID
     * @return 자기소개서 상세 정보
     */
    @GetMapping("/resume/{id}")
    public ResponseEntity<Map<String, Object>> getResumeDetail(@PathVariable Integer id) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        
        log.info("자기소개서 상세 조회 요청 - 사용자: {}, 자기소개서 ID: {}", email, id);
        
        try {
            // 삭제되지 않은 자기소개서만 조회
            Optional<Resume> resumeOpt = resumeRepository.findActiveResumeById(id);
            if (resumeOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            
            Resume resume = resumeOpt.get();
            
            // 사용자 본인의 자기소개서인지 확인
            if (!resume.getUser().getEmail().equals(email)) {
                log.warn("권한 없음: 사용자({})가 다른 사용자의 자기소개서({})에 접근 시도", email, id);
                return ResponseEntity.status(403).build();
            }
            
            Map<String, Object> resumeData = new HashMap<>();
            resumeData.put("id", resume.getResumeId());
            resumeData.put("title", resume.getResumeTitle());
            resumeData.put("resumeText", resume.getResumeText());
            resumeData.put("createdAt", resume.getCreatedAt());
            resumeData.put("updatedAt", resume.getUpdatedAt());
            
            log.info("자기소개서 상세 조회 완료 - 자기소개서 ID: {}", id);
            return ResponseEntity.ok(resumeData);
        } catch (Exception e) {
            log.error("자기소개서 상세 조회 실패: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 새로운 자기소개서를 생성합니다.
     * @param request 자기소개서 생성 요청
     * @return 생성된 자기소개서 정보
     */
    @PostMapping("/resume")
    public ResponseEntity<Map<String, Object>> createResume(@RequestBody Map<String, String> request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        
        log.info("자기소개서 생성 요청 - 사용자: {}", email);
        
        try {
            String title = request.get("title");
            String resumeText = request.get("resumeText");
            String resumeType = request.get("resumeType");
            
            if (title == null || title.trim().isEmpty()) {
                return ResponseEntity.badRequest().build();
            }
            
            User user = userService.findByEmail(email);
            
            Resume resume = new Resume();
            resume.setUser(user);
            resume.setResumeTitle(title);
            resume.setResumeText(resumeText != null ? resumeText : "");
            
            // resumeType 설정 (기본값은 text)
            if (resumeType != null) {
                try {
                    resume.setResumeType(ResumeType.valueOf(resumeType));
                } catch (IllegalArgumentException e) {
                    resume.setResumeType(ResumeType.text); // 기본값
                }
            } else {
                resume.setResumeType(ResumeType.text); // 기본값
            }
            
            Resume savedResume = resumeRepository.save(resume);
            
            Map<String, Object> response = new HashMap<>();
            response.put("id", savedResume.getResumeId());
            response.put("title", savedResume.getResumeTitle());
            response.put("resumeText", savedResume.getResumeText());
            response.put("createdAt", savedResume.getCreatedAt());
            response.put("updatedAt", savedResume.getUpdatedAt());
            
            log.info("자기소개서 생성 완료 - 자기소개서 ID: {}", savedResume.getResumeId());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("자기소개서 생성 실패: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 자기소개서를 수정합니다.
     * @param id 자기소개서 ID
     * @param request 수정할 내용
     * @return 수정된 자기소개서 정보
     */
    @PutMapping("/resume/{id}")
    public ResponseEntity<Map<String, Object>> updateResume(@PathVariable Integer id, @RequestBody Map<String, String> request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        
        log.info("자기소개서 수정 요청 - 사용자: {}, 자기소개서 ID: {}", email, id);
        
        try {
            // 삭제되지 않은 자기소개서만 수정 가능
            Optional<Resume> resumeOpt = resumeRepository.findActiveResumeById(id);
            if (resumeOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            
            Resume resume = resumeOpt.get();
            
            // 사용자 본인의 자기소개서인지 확인
            if (!resume.getUser().getEmail().equals(email)) {
                log.warn("권한 없음: 사용자({})가 다른 사용자의 자기소개서({})에 접근 시도", email, id);
                return ResponseEntity.status(403).build();
            }

            // 제목 수정 (제공된 경우에만)
            String title = request.get("title");
            if (title != null) {
                resume.setResumeTitle(title);
            }
            
            // 내용 수정 (제공된 경우에만)
            String resumeText = request.get("resumeText");
            if (resumeText != null) {
                resume.setResumeText(resumeText);
            }

            Resume savedResume = resumeRepository.save(resume);

            Map<String, Object> response = new HashMap<>();
            response.put("msg", "요청하신 자기소개서 수정이 성공적으로 완료되었습니다.");
            response.put("id", savedResume.getResumeId());
            response.put("title", savedResume.getResumeTitle());
            response.put("resumeText", savedResume.getResumeText());
            response.put("createdAt", savedResume.getCreatedAt());
            response.put("updatedAt", savedResume.getUpdatedAt());
            
            log.info("자기소개서 수정 완료 - 자기소개서 ID: {}", id);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("자기소개서 수정 실패: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 자기소개서를 휴지통으로 이동합니다 (소프트 삭제).
     * @param id 자기소개서 ID
     * @return 성공 여부
     */
    @PutMapping("/resume/{id}/soft-delete")
    public ResponseEntity<Void> softDeleteResume(@PathVariable Integer id) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        
        log.info("자기소개서 휴지통 이동 요청 - 사용자: {}, 자기소개서 ID: {}", email, id);
        
        try {
            // 삭제되지 않은 자기소개서만 휴지통으로 이동 가능
            Optional<Resume> resumeOpt = resumeRepository.findActiveResumeById(id);
            if (resumeOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            
            Resume resume = resumeOpt.get();
            
            // 사용자 본인의 자기소개서인지 확인
            if (!resume.getUser().getEmail().equals(email)) {
                log.warn("권한 없음: 사용자({})가 다른 사용자의 자기소개서({})에 접근 시도", email, id);
                return ResponseEntity.status(403).build();
            }
            
            // 현재 시간으로 deleted_at 설정 (소프트 삭제)
            resume.setDeletedAt(java.time.LocalDateTime.now());
            resumeRepository.save(resume);
            
            log.info("자기소개서 휴지통 이동 완료 - 자기소개서 ID: {}", id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("자기소개서 휴지통 이동 실패: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 휴지통의 자기소개서 목록을 조회합니다.
     * @return 휴지통 자기소개서 목록
     */
    @GetMapping("/resume/trash")
    public ResponseEntity<List<Map<String, Object>>> getResumeTrashList() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        
        log.info("자기소개서 휴지통 목록 조회 요청 - 사용자: {}", email);
        
        try {
            // 삭제된 자기소개서만 조회
            List<Resume> deletedResumes = resumeRepository.findDeletedResumesByUserEmail(email);
            
            // 최신순으로 정렬 (deletedAt 기준 내림차순, deletedAt이 null이면 createdAt 기준)
            deletedResumes.sort((a, b) -> {
                if (a.getDeletedAt() != null && b.getDeletedAt() != null) {
                    return b.getDeletedAt().compareTo(a.getDeletedAt());
                } else {
                    return b.getCreatedAt().compareTo(a.getCreatedAt());
                }
            });
            
            List<Map<String, Object>> resumeList = new ArrayList<>();
            
            for (Resume resume : deletedResumes) {
                Map<String, Object> resumeData = new HashMap<>();
                resumeData.put("id", resume.getResumeId());
                resumeData.put("title", resume.getResumeTitle());
                resumeData.put("resumeText", resume.getResumeText());
                resumeData.put("createdAt", resume.getCreatedAt());
                resumeData.put("updatedAt", resume.getUpdatedAt());
                resumeData.put("deletedAt", resume.getDeletedAt());
                resumeList.add(resumeData);
            }
            
            log.info("자기소개서 휴지통 목록 조회 완료 - 사용자: {}, 항목 수: {}", email, resumeList.size());
            return ResponseEntity.ok(resumeList);
        } catch (Exception e) {
            log.error("자기소개서 휴지통 목록 조회 실패: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 휴지통에서 자기소개서를 복구합니다.
     * @param id 자기소개서 ID
     * @return 성공 여부
     */
    @PutMapping("/resume/{id}/restore")
    public ResponseEntity<Void> restoreResume(@PathVariable Integer id) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        
        log.info("자기소개서 복구 요청 - 사용자: {}, 자기소개서 ID: {}", email, id);
        
        try {
            // 삭제된 자기소개서만 복구 가능
            Optional<Resume> resumeOpt = resumeRepository.findDeletedResumeById(id);
            if (resumeOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            
            Resume resume = resumeOpt.get();
            
            // 사용자 본인의 자기소개서인지 확인
            if (!resume.getUser().getEmail().equals(email)) {
                log.warn("권한 없음: 사용자({})가 다른 사용자의 자기소개서({})에 접근 시도", email, id);
                return ResponseEntity.status(403).build();
            }
            
            // deleted_at을 null로 설정하여 복구
            resume.setDeletedAt(null);
            resumeRepository.save(resume);
            
            log.info("자기소개서 복구 완료 - 자기소개서 ID: {}", id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("자기소개서 복구 실패: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 자기소개서를 완전히 삭제합니다 (하드 삭제).
     * @param id 자기소개서 ID
     * @return 성공 여부
     */
    @DeleteMapping("/resume/{id}")
    public ResponseEntity<Void> deleteResume(@PathVariable Integer id) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        
        log.info("자기소개서 완전 삭제 요청 - 사용자: {}, 자기소개서 ID: {}", email, id);
        
        try {
            // 휴지통에 있는 자기소개서만 완전 삭제 가능
            Optional<Resume> resumeOpt = resumeRepository.findDeletedResumeById(id);
            if (resumeOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            
            Resume resume = resumeOpt.get();
            
            // 사용자 본인의 자기소개서인지 확인
            if (!resume.getUser().getEmail().equals(email)) {
                log.warn("권한 없음: 사용자({})가 다른 사용자의 자기소개서({})에 접근 시도", email, id);
                return ResponseEntity.status(403).build();
            }
            
            // resume 테이블에서 삭제 (외래키 제약조건에 의해 resume_eval의 resume_id는 자동으로 NULL로 설정됨)
            resumeRepository.delete(resume);
            log.info("자기소개서 삭제 완료 - 관련 평가의 resume_id는 NULL로 설정됨");
            
            log.info("자기소개서 완전 삭제 완료 - 자기소개서 ID: {}", id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("자기소개서 완전 삭제 실패: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 사용자의 모든 자기소개서를 휴지통으로 이동합니다.
     * @return 성공 여부
     */
    @PutMapping("/resume/soft-delete-all")
    public ResponseEntity<Void> softDeleteAllResumes() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        
        log.info("모든 자기소개서 휴지통 이동 요청 - 사용자: {}", email);
        
        try {
            List<Resume> resumes = resumeRepository.findActiveResumesByUserEmail(email);
            
            // 모든 자기소개서를 휴지통으로 이동
            for (Resume resume : resumes) {
                resume.setDeletedAt(java.time.LocalDateTime.now());
            }
            resumeRepository.saveAll(resumes);
            
            log.info("모든 자기소개서 휴지통 이동 완료 - 사용자: {}, 이동된 항목 수: {}", email, resumes.size());
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("모든 자기소개서 휴지통 이동 실패: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 휴지통의 모든 자기소개서를 완전히 삭제합니다.
     * @return 성공 여부
     */
    @DeleteMapping("/resume/trash")
    public ResponseEntity<Void> deleteAllTrashResumes() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        
        log.info("휴지통의 모든 자기소개서 완전 삭제 요청 - 사용자: {}", email);
        
        try {
            List<Resume> deletedResumes = resumeRepository.findDeletedResumesByUserEmail(email);
            resumeRepository.deleteAll(deletedResumes);
            
            log.info("휴지통의 모든 자기소개서 완전 삭제 완료 - 사용자: {}, 삭제된 항목 수: {}", email, deletedResumes.size());
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("휴지통의 모든 자기소개서 완전 삭제 실패: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 자소서 평가의 타이틀을 수정합니다.
     * @param id 자소서 평가 ID
     * @param request 수정할 타이틀 정보
     * @return 성공 여부
     */
    @PutMapping("/evaluations/{id}/title")
    public ResponseEntity<Map<String, Object>> updateEvaluationTitle(@PathVariable Integer id, @RequestBody Map<String, String> request, @RequestParam(required = false) String type) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        
        String newTitle = request.get("title");
        if (newTitle == null || newTitle.trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        
        log.info("평가 타이틀 수정 요청 - 사용자: {}, 평가 ID: {}, 새 타이틀: {}, 타입: {}", email, id, newTitle, type);
        
        try {
            // 타입이 명시적으로 지정된 경우 해당 타입에서만 조회
            if ("voice".equals(type)) {
                Optional<Voice> voiceOpt = voiceRepository.findById(id);
                if (voiceOpt.isPresent()) {
                    Voice voice = voiceOpt.get();
                    
                    // 사용자 본인의 음성 평가인지 확인
                    if (!voice.getUser().getEmail().equals(email)) {
                        log.warn("권한 없음: 사용자({})가 다른 사용자의 음성 평가({})에 접근 시도", email, id);
                        return ResponseEntity.status(403).build();
                    }
                    
                    // 음성 평가의 경우 Voice 테이블의 file_name 수정
                    voice.setFileName(newTitle.trim());
                    voiceRepository.save(voice);
                    
                    Map<String, Object> response = new HashMap<>();
                    response.put("success", true);
                    response.put("message", "음성 평가 제목이 수정되었습니다.");
                    response.put("newTitle", newTitle.trim());
                    
                    log.info("음성 평가 제목 수정 완료 - 평가 ID: {}", id);
                    return ResponseEntity.ok(response);
                }
                return ResponseEntity.notFound().build();
            } else if ("resume".equals(type)) {
                Optional<ResumeEval> resumeEvalOpt = resumeEvalRepository.findById(id);
                if (resumeEvalOpt.isPresent()) {
                    ResumeEval resumeEval = resumeEvalOpt.get();
                    
                    // 사용자 본인의 평가인지 확인
                    if (!resumeEval.getUser().getEmail().equals(email)) {
                        log.warn("권한 없음: 사용자({})가 다른 사용자의 자소서 평가({})에 접근 시도", email, id);
                        return ResponseEntity.status(403).build();
                    }
                    
                    // 자소서 평가의 경우 ResumeEval 테이블의 resume_eval_title 수정
                    resumeEval.setResumeEvalTitle(newTitle.trim());
                    resumeEvalRepository.save(resumeEval);
                    
                    Map<String, Object> response = new HashMap<>();
                    response.put("success", true);
                    response.put("message", "자소서 평가 타이틀이 수정되었습니다.");
                    response.put("newTitle", newTitle.trim());
                    
                    log.info("자소서 평가 타이틀 수정 완료 - 평가 ID: {}", id);
                    return ResponseEntity.ok(response);
                }
                return ResponseEntity.notFound().build();
            } else if ("interview".equals(type)) {
                Optional<InterviewArchive> archiveOpt = interviewArchiveRepository.findById(id);
                if (archiveOpt.isPresent()) {
                    InterviewArchive archive = archiveOpt.get();
                    
                    // 사용자 본인의 평가인지 확인
                    if (!archive.getUser().getEmail().equals(email)) {
                        log.warn("권한 없음: 사용자({})가 다른 사용자의 평가({})에 접근 시도", email, id);
                        return ResponseEntity.status(403).build();
                    }
                    
                    // 면접 평가 타이틀 수정
                    archive.setArchive_name(newTitle.trim());
                    archive.setUpdatedAt(java.time.LocalDateTime.now());
                    interviewArchiveRepository.save(archive);
                    
                    Map<String, Object> response = new HashMap<>();
                    response.put("success", true);
                    response.put("message", "면접 평가 타이틀이 수정되었습니다.");
                    response.put("newTitle", newTitle.trim());
                    
                    log.info("면접 평가 타이틀 수정 완료 - 평가 ID: {}", id);
                    return ResponseEntity.ok(response);
                }
                return ResponseEntity.notFound().build();
            }
            
            // 타입이 지정되지 않은 경우 기존 로직 (하위 호환성)
            // 먼저 면접 평가에서 조회
            Optional<InterviewArchive> archiveOpt = interviewArchiveRepository.findById(id);
            if (archiveOpt.isPresent()) {
                InterviewArchive archive = archiveOpt.get();
                
                // 사용자 본인의 평가인지 확인
                if (!archive.getUser().getEmail().equals(email)) {
                    log.warn("권한 없음: 사용자({})가 다른 사용자의 평가({})에 접근 시도", email, id);
                    return ResponseEntity.status(403).build();
                }
                
                // 면접 평가 타이틀 수정
                archive.setArchive_name(newTitle.trim());
                archive.setUpdatedAt(java.time.LocalDateTime.now());
                interviewArchiveRepository.save(archive);
                
                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("message", "면접 평가 타이틀이 수정되었습니다.");
                response.put("newTitle", newTitle.trim());
                
                log.info("면접 평가 타이틀 수정 완료 - 평가 ID: {}", id);
                return ResponseEntity.ok(response);
            }
            
            // 면접 평가가 없으면 자소서 평가에서 조회
            Optional<ResumeEval> resumeEvalOpt = resumeEvalRepository.findById(id);
            if (resumeEvalOpt.isPresent()) {
                ResumeEval resumeEval = resumeEvalOpt.get();
                
                // 사용자 본인의 평가인지 확인
                if (!resumeEval.getUser().getEmail().equals(email)) {
                    log.warn("권한 없음: 사용자({})가 다른 사용자의 자소서 평가({})에 접근 시도", email, id);
                    return ResponseEntity.status(403).build();
                }
                
                // 자소서 평가의 경우 ResumeEval 테이블의 resume_eval_title 수정
                resumeEval.setResumeEvalTitle(newTitle.trim());
                resumeEvalRepository.save(resumeEval);
                
                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("message", "자소서 평가 타이틀이 수정되었습니다.");
                response.put("newTitle", newTitle.trim());
                
                log.info("자소서 평가 타이틀 수정 완료 - 평가 ID: {}", id);
                return ResponseEntity.ok(response);
            }
            
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("평가 타이틀 수정 실패: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
} 