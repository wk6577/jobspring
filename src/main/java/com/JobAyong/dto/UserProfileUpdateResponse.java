package com.JobAyong.dto;

import lombok.Data;

import java.time.LocalDate;

// 마이페이지 정보 수정 후 응답 양식
@Data
public class UserProfileUpdateResponse {
    private String name;
    private LocalDate birth;
    private String phoneNumber;
    private String gender;
}
