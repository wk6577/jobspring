package com.JobAyong.dto;

import lombok.Data;

import java.time.LocalDate;

// 마이페이지 정보 수정 요청 양식
@Data
public class UserProfileUpdateRequest {
    private String name;
    private LocalDate birth;
    private String phoneNumber;
    private String gender;
}

