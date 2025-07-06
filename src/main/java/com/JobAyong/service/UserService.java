package com.JobAyong.service;

import com.JobAyong.config.JwtTokenProvider;
import com.JobAyong.constant.UserRole;
import com.JobAyong.constant.UserStatus;
import com.JobAyong.constant.Gender;
import com.JobAyong.dto.*;
import com.JobAyong.entity.User;
import com.JobAyong.repository.UserRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final EmailService emailService;

    @Transactional
    public void signUp(UserSignUpRequest request) {
        // 탈퇴한 사용자인지 먼저 확인
        Optional<User> deletedUser = userRepository.findByEmailAndDeleted(request.getEmail());
        if (deletedUser.isPresent()) {
            throw new RuntimeException("탈퇴한 회원입니다. 관리자에게 문의하여 계정 복구를 요청해주세요.");
        }
        
        // 활성 사용자 중에 이미 존재하는 이메일인지 확인
        if (userRepository.existsByEmailAndNotDeleted(request.getEmail())) {
            throw new RuntimeException("이미 존재하는 이메일입니다.");
        }

        String encodedPassword = passwordEncoder.encode(request.getPassword());

        User user = new User();
        user.setEmail(request.getEmail());
        user.setPassword(encodedPassword);
        user.setName(request.getName());
        user.setBirth(request.getBirth());
        user.setPhoneNumber(request.getPhoneNumber());
        user.setGender(request.getGender());
        user.setRole(UserRole.USER.getValue());

        userRepository.save(user);
        
        // 환영 이메일 발송 (실패해도 회원가입은 완료)
        try {
            emailService.sendWelcomeEmail(request.getEmail(), request.getName());
        } catch (Exception e) {
            log.warn("환영 이메일 발송 실패: {}", e.getMessage());
            // 환영 이메일 실패는 회원가입을 막지 않음
        }
    }

    @Transactional(readOnly = true)
    public LoginResponse login(String email, String password) {
        // 탈퇴하지 않은 사용자만 로그인 가능
        User user = userRepository.findByEmailAndNotDeleted(email)
                .orElseThrow(() -> {
                    // 탈퇴한 사용자인지 확인
                    Optional<User> deletedUser = userRepository.findByEmailAndDeleted(email);
                    if (deletedUser.isPresent()) {
                        return new RuntimeException("탈퇴한 회원입니다. 관리자에게 문의하여 계정 복구를 요청해주세요.");
                    }
                    return new RuntimeException("존재하지 않는 이메일입니다.");
                });

        log.info("Login attempt - Email: {}", email);

        if (!passwordEncoder.matches(password, user.getPassword())) {
            log.error("Password mismatch for user: {}", email);
            throw new RuntimeException("비밀번호가 일치하지 않습니다.");
        }

        log.info("Login successful for user: {}", email);

        // JWT 토큰 생성
        String token = jwtTokenProvider.generateToken(user.getEmail(), user.getName());

        LoginResponse response = new LoginResponse();
        response.setName(user.getName());
        response.setToken(token);
        response.setRole(user.getUserRole());
        return response;
    }

    public User findByEmail(String email) {
        return userRepository.findByEmailAndNotDeleted(email)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
    }

    public boolean existsByEmail(String email) {
        return userRepository.existsByEmailAndNotDeleted(email);
    }

    /*@apiNote 유저 회사, 직무 업데이트용 함수
    * @author 나세호
    * */
    @Transactional
    public UserUpdateResponse updateUser(User updatedUser, UserUpdateRequest request) throws JsonProcessingException {

        ObjectMapper objectMapper = new ObjectMapper();

        String companyJson = objectMapper.writeValueAsString(request.getCompany());
        String jobJson = objectMapper.writeValueAsString(request.getJob());

        updatedUser.setCompany(companyJson);
        updatedUser.setJob(jobJson);

        userRepository.save(updatedUser);

        UserUpdateResponse response = new UserUpdateResponse();
        response.setMsg("회원 직무 / 회사 수정 성공!!");

        return  response;
    }

    @Transactional
    public void changePassword(String email, PasswordChangeRequest request) {
        User user = userRepository.findByEmailAndNotDeleted(email)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        // 현재 비밀번호 확인
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new RuntimeException("현재 비밀번호가 일치하지 않습니다.");
        }

        // 새 비밀번호와 확인 비밀번호 일치 확인
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new RuntimeException("새 비밀번호와 확인 비밀번호가 일치하지 않습니다.");
        }

        // 새 비밀번호 유효성 검사
        if (request.getNewPassword().length() < 6) {
            throw new RuntimeException("새 비밀번호는 6자 이상이어야 합니다.");
        }

        // 현재 비밀번호와 새 비밀번호가 같은지 확인
        if (passwordEncoder.matches(request.getNewPassword(), user.getPassword())) {
            throw new RuntimeException("새 비밀번호는 현재 비밀번호와 달라야 합니다.");
        }

        // 새 비밀번호 암호화 후 저장
        String encodedNewPassword = passwordEncoder.encode(request.getNewPassword());
        user.setPassword(encodedNewPassword);
        userRepository.save(user);

        log.info("비밀번호 변경 완료: {}", email);
    }

    @Transactional(readOnly = true)
    public User whoareyou(String email){ 
        return userRepository.findByEmailAndNotDeleted(email)
                .orElseThrow(() -> new RuntimeException("접근 권한이 없습니다.")); 
    }

    @Transactional
    public void delete(String email) {
        userRepository.deleteById(email);
    }

    /**
     * 회원탈퇴 - soft delete 방식
     * deleted_at 필드에 현재 시간을 설정하고 status를 DELETE_WAITING으로 변경
     */
    @Transactional
    public void withdrawUser(String email) {
        User user = userRepository.findByEmailAndNotDeleted(email)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
        
        user.setDeletedAt(LocalDateTime.now());
        user.setUserStatus(UserStatus.DELETE_WAITING);
        userRepository.save(user);
        
        log.info("회원탈퇴 완료: {} (상태: DELETE_WAITING)", email);
    }

    /**
     * 관리자용 - 탈퇴한 회원 복구
     */
    @Transactional
    public void restoreUser(String email) {
        User user = userRepository.findByEmailAndDeleted(email)
                .orElseThrow(() -> new RuntimeException("탈퇴한 사용자를 찾을 수 없습니다."));
        
        user.setDeletedAt(null);
        user.setUserStatus(UserStatus.ACTIVE);
        userRepository.save(user);
        
        log.info("회원 복구 완료: {} (상태: ACTIVE)", email);
    }

    /**
     * 관리자용 - 사용자 상태 변경 (활성/정지)
     */
    @Transactional
    public void updateUserStatus(String email, String status) {
        User user = userRepository.findByEmailAndNotDeleted(email)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
        
        UserStatus userStatus = UserStatus.fromString(status);
        user.setUserStatus(userStatus);
        userRepository.save(user);
        
        log.info("사용자 상태 변경 완료: {} -> {}", email, status);
    }

    /**
     * 관리자용 - 모든 사용자 목록 조회 (탈퇴한 사용자 포함)
     */
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getAllUsersForAdmin() {
        log.info("관리자용 전체 사용자 목록 조회 시작");
        
        List<User> allUsers = userRepository.findAll();
        log.info("데이터베이스에서 {} 명의 사용자 조회됨", allUsers.size());
        
        List<Map<String, Object>> userList = new ArrayList<>();
        
        for (User user : allUsers) {
            Map<String, Object> userInfo = new HashMap<>();
            userInfo.put("email", user.getEmail());
            userInfo.put("name", user.getName());
            userInfo.put("role", user.getUserRole());
            userInfo.put("status", user.getStatus() != null ? user.getStatus() : "ACTIVE");
            userInfo.put("createdAt", user.getCreatedAt());
            userInfo.put("deletedAt", user.getDeletedAt());
            userInfo.put("isDeleted", user.getDeletedAt() != null);
            userList.add(userInfo);
        }
        
        long activeUsers = userList.stream().filter(u -> !(Boolean)u.get("isDeleted")).count();
        long deletedUsers = userList.stream().filter(u -> (Boolean)u.get("isDeleted")).count();
        log.info("사용자 목록 변환 완료: 활성 사용자 {} 명, 탈퇴 사용자 {} 명", activeUsers, deletedUsers);
        
        return userList;
    }

    @Transactional
    public User updateUser(String email, UserProfileUpdateRequest request) {
        User user = userRepository.findByEmailAndNotDeleted(email)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        // 수정할 필드들 업데이트
        if (request.getName() != null && !request.getName().trim().isEmpty()) {
            user.setName(request.getName().trim());
        }

        if (request.getBirth() != null) {
            user.setBirth(request.getBirth());
        }

        if (request.getPhoneNumber() != null && !request.getPhoneNumber().trim().isEmpty()) {
            user.setPhoneNumber(request.getPhoneNumber().trim());
        }

        if (request.getGender() != null && !request.getGender().trim().isEmpty()) {
            // 문자열을 Gender enum으로 변환
            try {
                Gender gender = Gender.valueOf(request.getGender().toLowerCase());
                user.setGender(gender);
            } catch (IllegalArgumentException e) {
                throw new RuntimeException("유효하지 않은 성별 값입니다: " + request.getGender());
            }
        }

        return userRepository.save(user);
    }

    @Transactional
    public ProfileImageUploadResponse updateProfileImage(String email, MultipartFile file){
        try {
            // 1. 유저 조회
            User user = userRepository.findByEmailAndNotDeleted(email)
                    .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

            // 2. 파일 유효성 검사
            if (file.isEmpty()) {
                throw new RuntimeException("업로드된 파일이 비어있습니다.");
            }

            String originalFilename = file.getOriginalFilename();
            if (originalFilename == null || originalFilename.trim().isEmpty()) {
                throw new RuntimeException("파일명이 유효하지 않습니다.");
            }

            // 3. 파일 타입 검사 (이미지 파일만 허용)
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                throw new RuntimeException("이미지 파일만 업로드 가능합니다.");
            }

            // 4. 파일 크기 검사 (10MB 제한)
            if (file.getSize() > 10 * 1024 * 1024) {
                throw new RuntimeException("파일 크기는 10MB를 초과할 수 없습니다.");
            }

            // 5. 파일을 바이너리 데이터로 변환하여 DB에 저장
            byte[] imageData = file.getBytes();
            user.setProfileImage(imageData);
            user.setOriginalFilename(originalFilename);

            userRepository.save(user);

            // 6. 응답 생성 (Base64로 인코딩된 이미지 데이터 URL 반환)
            String base64Image = Base64.getEncoder().encodeToString(imageData);
            String imageUrl = "data:" + contentType + ";base64," + base64Image;

            return new ProfileImageUploadResponse(originalFilename, null, imageUrl, true, "프로필 이미지가 성공적으로 업로드되었습니다.");

        } catch (IOException e) {
            throw new RuntimeException("이미지 저장 실패: " + e.getMessage());
        }
    }

    /**
     * 이메일 찾기 - 이름, 생년월일, 전화번호로 이메일 조회
     */
    @Transactional(readOnly = true)
    public String findEmailByUserInfo(String name, String birth, String phoneNumber) {
        // birth 문자열을 LocalDate로 변환
        LocalDate birthDate;
        try {
            birthDate = LocalDate.parse(birth);
        } catch (Exception e) {
            throw new RuntimeException("생년월일 형식이 올바르지 않습니다. (예: 2001-01-23)");
        }
        
        User user = userRepository.findByNameAndBirthAndPhoneNumberAndNotDeleted(name, birthDate, phoneNumber)
                .orElseThrow(() -> new RuntimeException("입력하신 정보와 일치하는 사용자를 찾을 수 없습니다."));
        
        String originalEmail = user.getEmail();
        String maskedEmail = maskEmail(originalEmail);
        
        log.info("이메일 찾기 요청 - 이름: {}, 생년월일: {}, 전화번호: {}, 찾은 이메일: {}", 
                name, birth, phoneNumber, originalEmail);
        
        return maskedEmail;
    }

    /**
     * 이메일 마스킹 처리
     * 예: test@example.com -> t***@example.com
     *     user123@gmail.com -> u******@gmail.com
     */
    private String maskEmail(String email) {
        if (email == null || email.isEmpty()) {
            return email;
        }
        
        int atIndex = email.indexOf('@');
        if (atIndex <= 0) {
            return email; // @가 없거나 첫 번째 문자인 경우 원본 반환
        }
        
        String localPart = email.substring(0, atIndex);
        String domainPart = email.substring(atIndex);
        
        if (localPart.length() <= 1) {
            return email; // 로컬 파트가 1자 이하인 경우 원본 반환
        }
        
        // 첫 번째 문자는 그대로 두고, 나머지는 *로 마스킹
        String maskedLocalPart = localPart.charAt(0) + "*".repeat(localPart.length() - 1);
        
        return maskedLocalPart + domainPart;
    }

    /**
     * 비밀번호 재설정 요청 - 이메일로 재설정 토큰 생성 및 발송
     */
    @Transactional
    public void requestPasswordReset(String email) {
        User user = userRepository.findByEmailAndNotDeleted(email)
                .orElseThrow(() -> new RuntimeException("존재하지 않는 이메일입니다."));
        
        // 비밀번호 재설정 토큰 생성 (JWT 기반, 1시간 유효)
        String resetToken = jwtTokenProvider.generateResetToken(user.getEmail());
        
        // 이메일 발송
        try {
            emailService.sendPasswordResetEmail(email, resetToken);
            log.info("비밀번호 재설정 이메일 발송 완료: {}", email);
        } catch (Exception e) {
            log.error("비밀번호 재설정 이메일 발송 실패: {}", e.getMessage());
            throw new RuntimeException("이메일 발송에 실패했습니다. 잠시 후 다시 시도해주세요.");
        }
    }

    /**
     * 비밀번호 재설정 토큰 검증
     */
    @Transactional(readOnly = true)
    public boolean verifyResetToken(String token) {
        try {
            // JWT 토큰 검증
            if (jwtTokenProvider.validateResetToken(token)) {
                String email = jwtTokenProvider.getEmailFromResetToken(token);
                // 이메일이 존재하는지 확인
                return userRepository.existsByEmailAndNotDeleted(email);
            }
            return false;
        } catch (Exception e) {
            log.error("토큰 검증 실패: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 비밀번호 재설정
     */
    @Transactional
    public void resetPassword(String token, String newPassword, String confirmPassword) {
        // 토큰 검증
        if (!jwtTokenProvider.validateResetToken(token)) {
            throw new RuntimeException("만료되었거나 유효하지 않은 토큰입니다.");
        }
        
        // 새 비밀번호와 확인 비밀번호 일치 확인
        if (!newPassword.equals(confirmPassword)) {
            throw new RuntimeException("새 비밀번호와 확인 비밀번호가 일치하지 않습니다.");
        }
        
        // 새 비밀번호 유효성 검사
        if (newPassword.length() < 8) {
            throw new RuntimeException("새 비밀번호는 8자 이상이어야 합니다.");
        }
        
        // 문자와 숫자 포함 여부 확인
        if (!newPassword.matches(".*[A-Za-z].*")) {
            throw new RuntimeException("새 비밀번호는 문자를 포함해야 합니다.");
        }
        if (!newPassword.matches(".*\\d.*")) {
            throw new RuntimeException("새 비밀번호는 숫자를 포함해야 합니다.");
        }
        
        // 이메일 추출
        String email = jwtTokenProvider.getEmailFromResetToken(token);
        
        // 사용자 조회
        User user = userRepository.findByEmailAndNotDeleted(email)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
        
        // 새 비밀번호 암호화 후 저장
        String encodedNewPassword = passwordEncoder.encode(newPassword);
        user.setPassword(encodedNewPassword);
        userRepository.save(user);
        
        log.info("비밀번호 재설정 완료: {}", email);
    }
} 