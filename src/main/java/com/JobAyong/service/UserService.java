package com.JobAyong.service;

import com.JobAyong.config.JwtTokenProvider;
import com.JobAyong.constant.UserRole;
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

import java.io.File;
import java.io.IOException;
import java.util.UUID;

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
        if (userRepository.existsByEmail(request.getEmail())) {
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
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("존재하지 않는 이메일입니다."));

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

        return response;
    }

    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
    }

    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
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
        User user = userRepository.findByEmail(email)
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
    public User whoareyou(String email){ return userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("접근 권한이 없습니다.")); }

    @Transactional
    public void delete(String email) {
        userRepository.deleteById(email);
    }



    @Transactional
    public User updateUser(String email, UserProfileUpdateRequest request) {
        User user = userRepository.findByEmail(email)
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
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

            // 2. 파일 관련 정보
            String originalFilename = file.getOriginalFilename();
            String fileExt = originalFilename.substring(originalFilename.lastIndexOf("."));
            String savedFilename = UUID.randomUUID().toString() + fileExt;

            // 3. 저장 경로
            String uploadPath = "D:/T2/upload/image/";
            File dir = new File(uploadPath);
            if (!dir.exists()) dir.mkdirs();

            String fullPath = uploadPath + savedFilename;

            // 4. 실제 저장
            file.transferTo(new File(fullPath));

            // 5. DB 반영 (유저 정보에 저장된 파일명 추가)
            user.setProfileImage(savedFilename);
            user.setOriginalFilename(originalFilename);

            userRepository.save(user); // 저장

            // 6. 응답 생성
            return new ProfileImageUploadResponse(originalFilename, savedFilename, "/images/" + savedFilename); // 프론트에서 접근할 수 있는 URL 경로로 지정

        } catch (IOException e) {
            throw new RuntimeException("이미지 저장 실패: " + e.getMessage());
        }
    }
} 