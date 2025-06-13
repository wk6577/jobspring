package com.JobAyong.service;

import com.JobAyong.constant.UserRole;
import com.JobAyong.dto.LoginResponse;
import com.JobAyong.dto.UserSignUpRequest;
import com.JobAyong.entity.User;
import com.JobAyong.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

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
        log.info("Stored password: {}", user.getPassword());
        log.info("Input password: {}", password);

        if (!passwordEncoder.matches(password, user.getPassword())) {
            log.error("Password mismatch for user: {}", email);
            throw new RuntimeException("비밀번호가 일치하지 않습니다.");
        }

        log.info("Login successful for user: {}", email);

        LoginResponse response = new LoginResponse();
        response.setEmail(user.getEmail());
        response.setName(user.getName());
        response.setToken("dummy-token"); // TODO: JWT 토큰 생성 로직 추가

        return response;
    }

    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
    }

    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    @Transactional
    public void delete(String email) {
        userRepository.deleteById(email);
    }
} 