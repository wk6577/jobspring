package com.JobAyong.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class DashboardResponse {
    private String label;
    private Object value;
    private String icon;
}