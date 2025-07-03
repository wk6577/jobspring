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
            userInfo.put("role", user.getUserRole().getValue());
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

            return new ProfileImageUploadResponse(originalFilename, null, imageUrl);

        } catch (IOException e) {
            throw new RuntimeException("이미지 저장 실패: " + e.getMessage());
        }
    }
} 