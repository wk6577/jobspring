package com.JobAyong.controller;

import com.JobAyong.dto.PasswordChangeRequest;
import com.JobAyong.dto.UserSignUpRequest;
import com.JobAyong.dto.UserInfoResponse;
import com.JobAyong.dto.UserUpdateRequest;
import com.JobAyong.entity.User;
import com.JobAyong.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000") // React 개발 서버 주소
public class UserController {
    private final UserService userService;

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
                null // 프로필 이미지는 나중에 구현
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
                null // 프로필 이미지는 나중에 구현
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
} 