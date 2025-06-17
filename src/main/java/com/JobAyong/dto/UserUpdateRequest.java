package com.JobAyong.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class UserUpdateRequest {
    private String name;
    private LocalDate birth;
    private String phoneNumber;
    private String gender; // "male" 또는 "female"
    private String job; // 직무 정보 (JSON 문자열)
    private String company; // 회사 정보 (JSON 문자열)
} 