package com.JobAyong.dto;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class ProfileImageUploadRequest {
    private String email;
    private MultipartFile file;
}
