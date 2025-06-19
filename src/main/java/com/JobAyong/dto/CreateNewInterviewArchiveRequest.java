package com.JobAyong.dto;


import lombok.Data;

import java.util.List;

@Data
public class CreateNewInterviewArchiveRequest {
    private String archiveName;
    private String companyName;
    private String position;
    private List<String> questions;
    private String alternativeMode;
    private String archiveMode;
    private List<String> modes; // 안보내도 null로 허용
}
