package com.JobAyong.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserInfoResponse {
    private String email;
    private String name;
    private String birth;
    private String phoneNumber;
    private String gender;
    private String profileImage;
    private String job; // 직무 정보 (JSON 문자열)
    private String company; // 회사 정보 (JSON 문자열)
} 