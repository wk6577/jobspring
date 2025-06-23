package com.JobAyong.dto;

import com.JobAyong.constant.UserRole;
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
    private String profile;
    private String job;
    private String company;
    private UserRole role;
    private String status;
}