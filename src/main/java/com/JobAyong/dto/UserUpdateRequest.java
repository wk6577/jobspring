package com.JobAyong.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class UserUpdateRequest {
    private String name;
    private LocalDate birth;
    private String phoneNumber;
    private String gender; // "male" 또는 "female"
} 