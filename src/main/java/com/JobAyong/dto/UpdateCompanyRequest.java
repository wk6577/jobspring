package com.JobAyong.dto;

import com.JobAyong.constant.CompanySize;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Data
@Getter
@Setter
public class UpdateCompanyRequest {
    private Integer id; // 수정은 ID가 필수

    private String name;
    private CompanySize size;
    private String industry;
    private int employees;
    private LocalDate establishment;
    private String ceo;
    private String revenue;
    private String address;
    private String homepage;
    private String history;
    private String mainBusiness;
}
