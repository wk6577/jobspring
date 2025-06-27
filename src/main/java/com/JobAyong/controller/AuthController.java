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
} 