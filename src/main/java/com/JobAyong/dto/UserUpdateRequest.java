package com.JobAyong.dto;

import lombok.Data;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Data
public class UserUpdateRequest {
    private JobInfo job; // 직무 정보 (JSON 문자열)
    private CompanyInfo company; // 회사 정보 (JSON 문자열)

    @Data
    public static class JobInfo {
        private String category;
        private String subcategory;
        private String detail;
    }

    @Data
    public static class CompanyInfo {
        private String mainBusiness;
        private String companyName;
        private Long companyId;
    }
}