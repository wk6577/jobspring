package com.JobAyong.dto;

import com.JobAyong.constant.UserRole;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginResponse {
    private String token;
    private String name;
    private UserRole role;
} 