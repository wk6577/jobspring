package com.JobAyong.controller;

import com.JobAyong.dto.UserSignUpRequest;
import com.JobAyong.entity.User;
import com.JobAyong.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000") // React 개발 서버 주소
public class UserController {
    private final UserService userService;

    @PostMapping("/signup")
    public ResponseEntity<User> signUp(@RequestBody UserSignUpRequest request) {
        try {
            User user = userService.signUp(request);
            return ResponseEntity.ok(user);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/{email}")
    public ResponseEntity<User> getUserByEmail(@PathVariable String email) {
        try {
            User user = userService.findByEmail(email);
            return ResponseEntity.ok(user);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
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