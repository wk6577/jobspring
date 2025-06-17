package com.JobAyong.dto;

import lombok.Data;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Data
public class UserUpdateRequest {
    private String name;
    private String birth; // 문자열로 받아서 LocalDate로 변환
    private String phoneNumber;
    private String gender; // "male" 또는 "female"
    private String job; // 직무 정보 (JSON 문자열)
    private String company; // 회사 정보 (JSON 문자열)
    
    // birth 문자열을 LocalDate로 변환하는 메서드
    public LocalDate getBirthAsLocalDate() {
        if (birth == null || birth.trim().isEmpty()) {
            return null;
        }
        try {
            return LocalDate.parse(birth, DateTimeFormatter.ISO_LOCAL_DATE);
        } catch (Exception e) {
            throw new RuntimeException("유효하지 않은 날짜 형식입니다: " + birth);
        }
    }
} 