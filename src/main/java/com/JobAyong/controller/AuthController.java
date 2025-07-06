package com.JobAyong.controller;

import com.JobAyong.dto.LoginRequest;
import com.JobAyong.dto.LoginResponse;
import com.JobAyong.dto.UserInfoResponse;
import com.JobAyong.entity.User;
import com.JobAyong.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Base64;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
        LoginResponse response = userService.login(request.getEmail(), request.getPassword());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/me")
    public ResponseEntity<UserInfoResponse> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        
        User user = userService.findByEmail(email);

        // 프로필 이미지를 Base64로 변환
        String imageUrl = null;
        if (user.getProfileImage() != null && user.getProfileImage().length > 0) {
            String base64Image = Base64.getEncoder().encodeToString(user.getProfileImage());
            imageUrl = "data:image/jpeg;base64," + base64Image;
        }

        UserInfoResponse response = new UserInfoResponse();
        response.setEmail(user.getEmail());
        response.setName(user.getName());
        response.setBirth(user.getBirth().toString());
        response.setPhoneNumber(user.getPhoneNumber());
        response.setGender(user.getGender().name());
        response.setProfile(imageUrl);
        response.setJob(user.getJob());
        response.setCompany(user.getCompany());
        response.setRole(user.getUserRole());
        
        return ResponseEntity.ok(response);
    }

    @PostMapping("/find-email")
    public ResponseEntity<?> findEmail(@RequestBody FindEmailRequest request) {
        try {
            String maskedEmail = userService.findEmailByUserInfo(request.getName(), request.getBirth(), request.getPhoneNumber());
            return ResponseEntity.ok(new FindEmailResponse(true, "입력하신 정보로 등록된 이메일: " + maskedEmail, maskedEmail));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new FindEmailResponse(false, e.getMessage(), null));
        }
    }

    @PostMapping("/request-password-reset")
    public ResponseEntity<?> requestPasswordReset(@RequestBody PasswordResetRequest request) {
        try {
            userService.requestPasswordReset(request.getEmail());
            return ResponseEntity.ok(new PasswordResetResponse(true, "비밀번호 재설정 링크가 이메일로 발송되었습니다."));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new PasswordResetResponse(false, e.getMessage()));
        }
    }

    @GetMapping("/verify-reset-token")
    public ResponseEntity<?> verifyResetToken(@RequestParam String token) {
        try {
            boolean isValid = userService.verifyResetToken(token);
            if (isValid) {
                return ResponseEntity.ok(new TokenVerificationResponse(true, "유효한 토큰입니다."));
            } else {
                return ResponseEntity.badRequest().body(new TokenVerificationResponse(false, "만료되었거나 유효하지 않은 토큰입니다."));
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new TokenVerificationResponse(false, e.getMessage()));
        }
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody PasswordResetConfirmRequest request) {
        try {
            userService.resetPassword(request.getToken(), request.getNewPassword(), request.getConfirmPassword());
            return ResponseEntity.ok(new PasswordResetResponse(true, "비밀번호가 성공적으로 재설정되었습니다."));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new PasswordResetResponse(false, e.getMessage()));
        }
    }

    // DTO 클래스들
    public static class FindEmailRequest {
        private String name;
        private String birth;
        private String phoneNumber;

        // Getters and Setters
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getBirth() { return birth; }
        public void setBirth(String birth) { this.birth = birth; }
        public String getPhoneNumber() { return phoneNumber; }
        public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
    }

    public static class FindEmailResponse {
        private boolean success;
        private String message;
        private String email;

        public FindEmailResponse(boolean success, String message, String email) {
            this.success = success;
            this.message = message;
            this.email = email;
        }

        // Getters and Setters
        public boolean isSuccess() { return success; }
        public void setSuccess(boolean success) { this.success = success; }
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
    }

    public static class PasswordResetRequest {
        private String email;

        // Getters and Setters
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
    }

    public static class PasswordResetConfirmRequest {
        private String token;
        private String newPassword;
        private String confirmPassword;

        // Getters and Setters
        public String getToken() { return token; }
        public void setToken(String token) { this.token = token; }
        public String getNewPassword() { return newPassword; }
        public void setNewPassword(String newPassword) { this.newPassword = newPassword; }
        public String getConfirmPassword() { return confirmPassword; }
        public void setConfirmPassword(String confirmPassword) { this.confirmPassword = confirmPassword; }
    }

    public static class PasswordResetResponse {
        private boolean success;
        private String message;

        public PasswordResetResponse(boolean success, String message) {
            this.success = success;
            this.message = message;
        }

        // Getters and Setters
        public boolean isSuccess() { return success; }
        public void setSuccess(boolean success) { this.success = success; }
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
    }

    public static class TokenVerificationResponse {
        private boolean valid;
        private String message;

        public TokenVerificationResponse(boolean valid, String message) {
            this.valid = valid;
            this.message = message;
        }

        // Getters and Setters
        public boolean isValid() { return valid; }
        public void setValid(boolean valid) { this.valid = valid; }
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
    }
} 