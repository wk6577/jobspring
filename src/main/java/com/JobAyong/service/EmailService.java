package com.JobAyong.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${app.email.from}")
    private String fromEmail;

    @Value("${app.email.from-name}")
    private String fromName;

    /**
     * 비밀번호 재설정 이메일 발송
     */
    public void sendPasswordResetEmail(String toEmail, String resetToken) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("[JobAyong] 비밀번호 재설정");
            
            String emailContent = String.format(
                "안녕하세요, %s입니다.\n\n" +
                "비밀번호 재설정을 요청하셨습니다.\n\n" +
                "아래 링크를 클릭하여 비밀번호를 재설정해주세요:\n" +
                "https://jobayong.shop/reset-password?token=%s\n\n" +
                "이 링크는 1시간 후에 만료됩니다.\n\n" +
                "비밀번호 재설정을 요청하지 않으셨다면 이 이메일을 무시하셔도 됩니다.\n\n" +
                "감사합니다.\n" +
                "JobAyong 팀",
                fromName, resetToken
            );
            
            message.setText(emailContent);
            
            mailSender.send(message);
            log.info("비밀번호 재설정 이메일 발송 완료: {}", toEmail);
            
        } catch (Exception e) {
            log.error("비밀번호 재설정 이메일 발송 실패: {}", e.getMessage(), e);
            throw new RuntimeException("이메일 발송에 실패했습니다: " + e.getMessage());
        }
    }

    /**
     * 환영 이메일 발송 (회원가입 시)
     */
    public void sendWelcomeEmail(String toEmail, String userName) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("[JobAyong] 회원가입을 환영합니다!");
            
            String emailContent = String.format(
                "안녕하세요, %s님!\n\n" +
                "JobAyong에 가입해주셔서 감사합니다.\n\n" +
                "JobAyong은 AI 기반 면접 연습과 자기소개서 작성 도움을 제공하는 서비스입니다.\n\n" +
                "지금 바로 서비스를 이용해보세요!\n" +
                "http://localhost:3000\n\n" +
                "감사합니다.\n" +
                "JobAyong 팀",
                userName
            );
            
            message.setText(emailContent);
            
            mailSender.send(message);
            log.info("환영 이메일 발송 완료: {}", toEmail);
            
        } catch (Exception e) {
            log.error("환영 이메일 발송 실패: {}", e.getMessage(), e);
            // 환영 이메일은 실패해도 회원가입을 막지 않음
        }
    }
} 