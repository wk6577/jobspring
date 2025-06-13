package com.JobAyong.dto;

import com.JobAyong.constant.Gender;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDate;

@Getter
@Setter
public class UserSignUpRequest {
    private String email;
    private String password;
    private String name;
    private LocalDate birth;
    private String phoneNumber;
    private Gender gender;
} 