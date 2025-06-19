package com.JobAyong.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class ProfileImageUploadResponse {
    private String originalFilename;
    private String savedFilename;
    private String imageUrl;
}
