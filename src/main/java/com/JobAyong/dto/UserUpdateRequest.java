package com.JobAyong.dto;

import lombok.Data;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Data
public class UserUpdateRequest {
    private String job; // 직무 정보 (JSON 문자열)
    private Long company; // 회사 정보 (JSON 문자열)
} 