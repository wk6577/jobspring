package com.JobAyong.dto;


import lombok.Data;

import java.util.List;

@Data
public class createNewInterviewArchiveRequest {
    private String email;
    private String ArchiveName;
    private Long companyId;
    private String position;
    private List<String> questions;
    private String alternativeMode;
    private List<String> modes; // 안보내도 null로 허용
}
